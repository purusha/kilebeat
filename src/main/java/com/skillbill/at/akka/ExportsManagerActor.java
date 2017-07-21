package com.skillbill.at.akka;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.inject.Inject;
import com.skillbill.at.akka.dto.EndPointFailed;
import com.skillbill.at.configuration.ConfigurationValidator.SingleConfiguration;
import com.skillbill.at.guice.GuiceAbstractActor;
import com.skillbill.at.guice.GuiceActorUtils;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import lombok.extern.slf4j.Slf4j;
import scala.concurrent.duration.FiniteDuration;

import static com.skillbill.at.Utility.*;

@Slf4j
public class ExportsManagerActor extends GuiceAbstractActor {	
	private final static String SCHEDULATION_CHECK = "SchedulationsCheck";
		
	private final Map<ActorRef, List<EndPointFailed>> association;
	private final Cancellable schedule;

	@Inject
	public ExportsManagerActor() {
		this.association = new HashMap<>();
		
		final ActorSystem system = getContext().system();
		
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
			.match(EndPointFailed.class, f -> {			
				getFailed(getSender()).add(f);
			})
			.matchEquals(SCHEDULATION_CHECK, sc -> {	
				LOGGER.info("### check failure connnector {}", association.keySet());
				
				final Set<ActorRef> actorRefs = association.keySet();
				
				actorRefs.forEach(childActor -> {						
					final List<EndPointFailed> actorFails = association.get(childActor);
					LOGGER.info("### found {} failed conf for {}", actorFails.size(), childActor);
					
					for(int i = 0; i < actorFails.size(); i++) {						
						final EndPointFailed configuration = actorFails.get(i);
						
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
			.match(SingleConfiguration.class, c -> {
				//create child
				final ActorRef actorOf = getContext().actorOf(
					GuiceActorUtils.makeProps(getContext().system(), TailerActor.class), tailer()
				);
				
				//configure
				actorOf.tell(c, ActorRef.noSender());
				
				//getContext().watch(actorOf);				
			})
			.matchAny(o -> {
				LOGGER.warn("not handled message", o);
			})
			.build();							
	}

	private List<EndPointFailed> getFailed(ActorRef sender) {
		if (!association.containsKey(sender)) {
			association.put(sender, new ArrayList<>());
		}
		
		return association.get(sender);
	}
}
