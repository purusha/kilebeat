package com.skillbill.at.akka;

import static akka.actor.SupervisorStrategy.stop;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;

import com.skillbill.at.akka.dto.HttpEndPointConfiuration;
import com.skillbill.at.akka.dto.HttpEndPointFailed;
import com.skillbill.at.akka.dto.KafkaEndPointConfiuration;
import com.skillbill.at.akka.dto.NewLineEvent;
import com.skillbill.at.guice.GuiceAbstractActor;
import com.skillbill.at.guice.GuiceActorUtils;
import com.typesafe.config.Config;

import akka.actor.ActorRef;
import akka.actor.OneForOneStrategy;
import akka.actor.SupervisorStrategy;
import akka.actor.Terminated;
import akka.japi.pf.DeciderBuilder;
import akka.routing.ActorRefRoutee;
import akka.routing.BroadcastRoutingLogic;
import akka.routing.Routee;
import akka.routing.Router;
import lombok.extern.slf4j.Slf4j;
import scala.concurrent.duration.Duration;

@Slf4j
public class TailerActor extends GuiceAbstractActor implements TailerListener  {	
	
	private static final int DELAY = 50;
	private static final int BUFFER_SIZE = 1000;
	private static final boolean FROM_END = true;
	private static final boolean RE_OPEN = true;
	
	private File resource;
	private Router router;
//	private Config config;	

//	@Inject
//	public TailerActor() {
//		resource = new File("/Users/power/Tmp/a");
//		
//		Tailer.create(
//			resource, Charset.forName("UTF-8"), this, DELAY, FROM_END, RE_OPEN, BUFFER_SIZE
//		);		
//									
//		router = new Router(
//			new BroadcastRoutingLogic(),			
//			IntStream.range(1, 2)
//				.mapToObj(ic -> buildHttpActor(new HttpEndPointConfiuration("http://localhost:8888")))
//				.collect(Collectors.toList())			
//		);	
//	}

	private Routee buildHttpActor(HttpEndPointConfiuration conf) {
		
		//create
		final ActorRef actor = getContext().actorOf(
			GuiceActorUtils.makeProps(getContext().system(), HttpEndpointActor.class)
		);
		
		//configure
		actor.tell(conf, ActorRef.noSender());
					
		getContext().watch(actor); //to see Terminated event associated with this actor
		
		return new ActorRefRoutee(actor);
	}

	private Routee buildKafkaActor(KafkaEndPointConfiuration conf) {
		
		//create
		final ActorRef actor = getContext().actorOf(
			GuiceActorUtils.makeProps(getContext().system(), KafkaEndpointActor.class)
		);
		
		//configure
		actor.tell(conf, ActorRef.noSender());
					
		getContext().watch(actor); //to see Terminated event associated with this actor
		
		return new ActorRefRoutee(actor);
	}
	
	@Override
	public void postStop() throws Exception {
		super.postStop();
		
		LOGGER.info("############################################ " + getSelf().path());
	}
	
	@Override
	public void preStart() throws Exception {
		super.preStart();
		
		LOGGER.info("**************************************** " + getSelf().path());
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder()
			.match(NewLineEvent.class, s -> {
				LOGGER.info("[row] {}", s);
				
				router.route(s, ActorRef.noSender());
			})
			.match(Terminated.class, t -> {				
				final ActorRef fail = t.actor();
				LOGGER.warn("actor {} is terminated", fail);
				
				getContext().unwatch(fail);				
				router = router.removeRoutee(fail);																			
			})
			.match(HttpEndPointFailed.class, f -> {
				if (f.isExpired()) {
					LOGGER.info("expired {}", f);
					router = router.addRoutee(buildHttpActor(f.getConf()));
				} else {
					//LOGGER.info("NOT expired {}", f);
					getSelf().tell(f, ActorRef.noSender()); //NOT GOOD IDEA ... PLEASE REMOVE ME ASAP !!?
				}
			})
			.match(Config.class, c -> {
				//config = c;
				
				resource = new File(c.getString("path"));
				
				router = new Router(new BroadcastRoutingLogic());				
				
				final Config httpConfig = c.getObject("http").toConfig();
				final Config kafkaConfig = c.getObject("kafka").toConfig();
				
				if (!httpConfig.isEmpty()) {
					router = router.addRoutee(buildHttpActor(null));
				}
				
				if (!kafkaConfig.isEmpty()) {
					router = router.addRoutee(buildKafkaActor(null));
				}
								
			})
			.matchAny(o -> {
				LOGGER.warn("not handled message", o);
			})
			.build();
	}

	@Override
	public void init(Tailer tailer) {
	}

	@Override
	public void fileNotFound() {
	}

	@Override
	public void fileRotated() {
	}

	@Override
	public void handle(String line) {				
		getSelf().tell(
			new NewLineEvent(line, resource.toPath()), ActorRef.noSender()
		);
	}

	@Override
	public void handle(Exception ex) {
		LOGGER.error("[ => ]", ex);
		//getSelf().tell(ex, ActorRef.noSender());
	}
	
	@Override
	public SupervisorStrategy supervisorStrategy() {				
		//strategy is applied only to the child actor that failed
		return new OneForOneStrategy( 
			1, 
			Duration.create(1, TimeUnit.MINUTES), 
			DeciderBuilder.
				match(Exception.class, e -> stop()).
//	        	match(ArithmeticException.class, e -> resume()).
//	        	match(NullPointerException.class, e -> restart()).
//	        	match(IllegalArgumentException.class, e -> stop()).
//	        	matchAny(o -> escalate()).
				build()
        );		
	}
	
}
