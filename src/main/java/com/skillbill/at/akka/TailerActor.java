package com.skillbill.at.akka;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;

import com.google.inject.Inject;
import com.skillbill.at.NewLineEvent;
import com.skillbill.at.guice.GuiceAbstractActor;
import com.skillbill.at.guice.GuiceActorUtils;

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

import static akka.actor.SupervisorStrategy.*;

@Slf4j
public class TailerActor extends GuiceAbstractActor implements TailerListener  {	
	
	private static final int DELAY = 50;
	private static final int BUFFER_SIZE = 1000;
	private static final boolean FROM_END = true;
	private static final boolean RE_OPEN = true;
	
	private final Router router;
	private final File resource;	

	@Inject
	public TailerActor() {
		resource = new File("/home/developer/a");
		
		Tailer.create(
			resource, Charset.forName("UTF-8"), this, DELAY, FROM_END, RE_OPEN, BUFFER_SIZE
		);		
		
		final List<Routee> routees = new ArrayList<Routee>();	
		
		IntStream.range(1, 4).forEach(ic -> {
			
			//create
			final ActorRef actor = getContext().actorOf(
				GuiceActorUtils.makeProps(getContext().system(), HttpEndpointActor.class)
			);
			
			//configure
			actor.tell(new HttpEndPointConfiuration("http://localhost:4242" + ic), ActorRef.noSender());
						
			//getContext().watch(actor); //to see all event associated with this actor, 4 example the 'Terminated'
			
			//add
			routees.add(new ActorRefRoutee(actor));
			
		}); 
				
		router = new Router(new BroadcastRoutingLogic(), routees);	
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder()
			.match(NewLineEvent.class, s -> {
				LOGGER.info("[row] {}", s);
				
				router.route(s, getSender());
			})
//			.match(Terminated.class, t -> {
//				LOGGER.warn("actor {} is terminated", t.actor());								
//			})
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
	
//	@Override
//	public SupervisorStrategy supervisorStrategy() {		
//		
//		return new OneForOneStrategy(
//			1, 
//			Duration.create(1, TimeUnit.MINUTES), 
//			DeciderBuilder.
//				match(Exception.class, e -> stop()).
////	        	match(ArithmeticException.class, e -> resume()).
////	        	match(NullPointerException.class, e -> restart()).
////	        	match(IllegalArgumentException.class, e -> stop()).
////	        	matchAny(o -> escalate()).
//				build()
//        );
//		
//	}
	
}
