package com.skillbill.at;

import com.skillbill.at.akka.ExportsManagerActor;
import com.skillbill.at.akka.FileSystemWatcherActor;
import com.skillbill.at.akka.RetrieveActors;
import com.skillbill.at.guice.GuiceActorUtils;
import com.skillbill.at.guice.GuiceExtension;
import com.skillbill.at.guice.GuiceExtensionImpl;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.DeadLetter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KileBeatApplication {
	public void run() throws Exception {
		
        //create system
        final ActorSystem system = ActorSystem.create("kile", ConfigFactory.load());
        system.registerExtension(GuiceExtension.provider);
        
		//Add shutdownhook
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
	        LOGGER.info("-------------------------------------------------");
	        LOGGER.info(" KileBeat STOPPED");
	        LOGGER.info("-------------------------------------------------");
			
            system.terminate();
        }));

        //configure Guice
        final GuiceExtensionImpl guiceExtension = GuiceExtension.provider.get(system);
        guiceExtension.setInjector(StartSystem.injector);

        final ActorRef retrieveActor = system.actorOf(
    		GuiceActorUtils.makeProps(system, RetrieveActors.class), "retrieve"
		);        
        
        system.eventStream().subscribe(retrieveActor, DeadLetter.class);

        //XXX create before watcher because manager use watcher internally
        system.actorOf(
    		GuiceActorUtils.makeProps(system, ExportsManagerActor.class), "manager"
		);        
        
        system.actorOf(
    		GuiceActorUtils.makeProps(system, FileSystemWatcherActor.class), "watcher"
		);
                
        LOGGER.info("-------------------------------------------------");
        LOGGER.info(" KileBeat STARTED");
        LOGGER.info("-------------------------------------------------");

    }
}
