package com.skillbill.at.akka;

import static akka.actor.SupervisorStrategy.stop;

import java.io.File;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;

import com.skillbill.at.akka.dto.EndPointFailed;
import com.skillbill.at.akka.dto.HttpEndPointConfiuration;
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
public class TailerActor extends GuiceAbstractActor implements TailerListener {		
	private static final int DELAY = 50;
	private static final int BUFFER_SIZE = 1000;
	private static final boolean FROM_END = true;
	private static final boolean RE_OPEN = true;
	
	private File resource;
	private Router router;
	private Tailer tailer;
		
	@Override
	public void postStop() throws Exception {
		super.postStop();
		LOGGER.info("end {} ", getSelf().path());
		
		tailer.stop();
	}
	
	@Override
	public void preStart() throws Exception {
		super.preStart();
		LOGGER.info("start {} with parent {}", getSelf().path(), getContext().parent());
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
			.match(EndPointFailed.class, f -> {
				if (f.isExpired()) {
					LOGGER.info("expired {}", f);
										
					router = router.addRoutee(buildRoutee(
						f.getConf(),
						getContext().actorOf(
							GuiceActorUtils.makeProps(getContext().system(), f.isHttp() ? HttpEndpointActor.class : KafkaEndpointActor.class),
							(f.isHttp() ? "http" : "kafka") + f.getConf().hashCode()
						)											
					));
				} else {
					LOGGER.info("NOT expired {}", f);
					getContext().parent().tell(f, getSelf());
				}
			})
			.match(Config.class, c -> {
				resource = new File(c.getString("path"));				
				router = new Router(new BroadcastRoutingLogic());				

				//DRY
				final Config httpConfig = c.hasPath("http") ? c.getObject("http").toConfig() : null;								
				if (httpConfig != null && !httpConfig.isEmpty()) {					
					final HttpEndPointConfiuration conf = new HttpEndPointConfiuration(httpConfig.getString("url"));
					router = router.addRoutee(buildRoutee(
						conf,
						getContext().actorOf(
							GuiceActorUtils.makeProps(getContext().system(), HttpEndpointActor.class),
							"http" + conf.hashCode()
						)																	
					));
				}
				
				//DRY				
				final Config kafkaConfig = c.hasPath("kafka") ? c.getObject("kafka").toConfig() : null;
				if (kafkaConfig != null && !kafkaConfig.isEmpty()) {
					KafkaEndPointConfiuration conf = new KafkaEndPointConfiuration(kafkaConfig.getString("queue"));
					router = router.addRoutee(buildRoutee(
						conf,
						getContext().actorOf(
							GuiceActorUtils.makeProps(getContext().system(), KafkaEndpointActor.class),
							"kafka" + conf.hashCode()
						)												
					));
				}		
				
				tailer = Tailer.create(
					resource, Charset.forName("UTF-8"), this, DELAY, FROM_END, RE_OPEN, BUFFER_SIZE
				);						
			})
			.matchAny(o -> {
				LOGGER.warn("not handled message", o);
			})
			.build();
	}

	@Override
	public void init(Tailer tailer) { } /* avoid interface segregation pplease !!? */

	@Override
	public void fileNotFound() { } /* avoid interface segregation pplease !!? */

	@Override
	public void fileRotated() { } /* avoid interface segregation pplease !!? */

	@Override
	public void handle(String line) {				
		getSelf().tell(
			new NewLineEvent(line, resource.toPath()), ActorRef.noSender()
		);
	}

	@Override
	public void handle(Exception ex) {
		LOGGER.error("[ => ]", ex);
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

	private Routee buildRoutee(Object conf, ActorRef child) {		
		child.tell(conf, ActorRef.noSender()); //configure					
		getContext().watch(child); //to see Terminated event associated with 'child' actor
		
		return new ActorRefRoutee(child);		
	}	
}
