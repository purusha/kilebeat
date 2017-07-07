package com.skillbill.at.akka;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;
import com.skillbill.at.guice.GuiceAbstractActor;
import com.skillbill.at.guice.GuiceActorUtils;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExportsManagerActor extends GuiceAbstractActor {
	
	private final Config config;
	
	private final Map<ActorRef, Config> association;

	@Inject
	public ExportsManagerActor(Config config) {
		this.config = config;		
		this.association = new HashMap<>();
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
		
		final ActorSystem system = getContext().system();
		
		((List<ConfigObject>) config.getObjectList("exports")).forEach(obj -> {
			final Config c = obj.toConfig();			
			
			//create
			final ActorRef actorOf = system.actorOf(
				GuiceActorUtils.makeProps(system, TailerActor.class)
			);
			
			//configure
			actorOf.tell(c, ActorRef.noSender());
			
			//TODO watch actorOf ??
			
			association.put(actorOf, c);						
		});						
	}
	
	@Override
	public Receive createReceive() {
		return ReceiveBuilder.create().build();
	}

}
