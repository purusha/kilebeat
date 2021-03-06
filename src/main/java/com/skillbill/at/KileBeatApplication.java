package com.skillbill.at;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.skillbill.at.akka.BulkTimeoutActor;
import com.skillbill.at.akka.ExportsManagerActor;
import com.skillbill.at.akka.FileSystemWatcherActor;
import com.skillbill.at.akka.RetrieveActors;
import com.skillbill.at.guice.GuiceActorUtils;
import com.skillbill.at.guice.GuiceExtension;
import com.skillbill.at.guice.GuiceExtensionImpl;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorSystem;
import akka.actor.DeadLetter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KileBeatApplication {
	
	private Injector injector;

	@Inject
	public KileBeatApplication(Injector injector) {
		this.injector = injector;
	}
	
	public void run() throws Exception {
		
        //create system
        final ActorSystem system = ActorSystem.create("kile", ConfigFactory.load());        
        
		//Add shutdownhook
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
	        LOGGER.info("-------------------------------------------------");
	        LOGGER.info(" KileBeat STOPPED");
	        LOGGER.info("-------------------------------------------------");
			
            system.terminate();
        }));
		
		system.registerExtension(GuiceExtension.provider);

        //configure Guice
        final GuiceExtensionImpl guiceExtension = GuiceExtension.provider.get(system);
        guiceExtension.setInjector(injector);
        
        //XXX start only in development environment
        system
        	.eventStream()
        	.subscribe(
    	        system.actorOf(
	        		GuiceActorUtils.makeProps(system, RetrieveActors.class), "retrieve"
	    		), 
    			DeadLetter.class
			);

        //XXX create before watcher because ... manager use watcher internally
        system.actorOf(
    		GuiceActorUtils.makeProps(system, ExportsManagerActor.class), "manager"
		);        
        
        system.actorOf(
    		GuiceActorUtils.makeProps(system, FileSystemWatcherActor.class), "watcher"
		);

        system.actorOf(
    		GuiceActorUtils.makeProps(system, BulkTimeoutActor.class), "bulk-timeout"
		);

        LOGGER.info("-------------------------------------------------");
        LOGGER.info(" KileBeat STARTED");
        LOGGER.info("-------------------------------------------------");

    }
}
