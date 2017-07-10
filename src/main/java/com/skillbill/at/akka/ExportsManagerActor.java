package com.skillbill.at.akka;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.inject.Inject;
import com.skillbill.at.akka.dto.ConfigurationFailed;
import com.skillbill.at.akka.dto.HttpEndPointFailed;
import com.skillbill.at.akka.dto.KafkaEndPointFailed;
import com.skillbill.at.guice.GuiceAbstractActor;
import com.skillbill.at.guice.GuiceActorUtils;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import lombok.extern.slf4j.Slf4j;
import scala.concurrent.duration.FiniteDuration;

@Slf4j
public class ExportsManagerActor extends GuiceAbstractActor {	
	private final static String SCHEDULATION_CHECK = "SchedulationsCheck";
		
	private final Map<ActorRef, List<ConfigurationFailed>> association;
	private final Cancellable schedule;

	@Inject
	@SuppressWarnings("unchecked")
	public ExportsManagerActor(Config config) {
		this.association = new HashMap<>();
		
		final ActorSystem system = getContext().system();
		
		((List<ConfigObject>) config.getObjectList("exports")).forEach(obj -> {
			final Config c = obj.toConfig();			
			
			//create child
			final ActorRef actorOf = getContext().actorOf(
				GuiceActorUtils.makeProps(system, TailerActor.class)
			);
			
			//configure
			actorOf.tell(c, ActorRef.noSender());
			
			//getContext().watch(actorOf);
		});						
	
		schedule = system.scheduler().schedule(
			FiniteDuration.create(10, TimeUnit.SECONDS), 
			FiniteDuration.create(10, TimeUnit.SECONDS), 
			getSelf(), SCHEDULATION_CHECK, 
			system.dispatcher(), getSelf()
		);		
	}
	
	@Override
	public void postStop() throws Exception {
		super.postStop();
		
		LOGGER.info("end {} ", getSelf().path());
		
		schedule.cancel();
	}
	
	@Override
	public void preStart() throws Exception {
		super.preStart();
		
		LOGGER.info("start {} with parent {}", getSelf().path(), getContext().parent());
	}
	
	@Override
	public Receive createReceive() {
		return receiveBuilder()
			.match(HttpEndPointFailed.class, f -> {			
				getFailed(getSender()).add(f);
				
				//LOGGER.info("### HttpEndPointFailed ### {}", association);
			})
			.match(KafkaEndPointFailed.class, f -> {
				getFailed(getSender()).add(f);
				
				//LOGGER.info("### KafkaEndPointFailed ### {}", association);
			})
			.matchEquals(SCHEDULATION_CHECK, sc -> {	
				LOGGER.info("### chec ### {}", association);
				
				final Set<ActorRef> actorRefs = association.keySet();
				
				actorRefs.forEach(childActor -> {						
					final List<ConfigurationFailed> actorFails = association.get(childActor);
					LOGGER.info("found {} failed conf for {}", actorFails.size(), childActor);
					
					for(int i = 0; i < actorFails.size(); i++) {						
						final ConfigurationFailed configuration = actorFails.get(i);
						
						if (configuration.isExpired()) {														
							childActor.tell(configuration, ActorRef.noSender());
							actorFails.remove(i);
						}						
					}
				});
				
				//remove key's without values!!
				actorRefs.removeAll(
					actorRefs.stream().filter(childActor -> association.get(childActor).isEmpty()).collect(Collectors.toList())
				);
			})
			.matchAny(o -> {
				LOGGER.warn("not handled message", o);
			})
			.build();							
	}

	private List<ConfigurationFailed> getFailed(ActorRef sender) {
		if (!association.containsKey(getSender())) {
			association.put(sender, new ArrayList<>());
		}
		
		return association.get(sender);
	}
}
