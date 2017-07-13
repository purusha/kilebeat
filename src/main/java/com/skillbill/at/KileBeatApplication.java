package com.skillbill.at;

import com.skillbill.at.akka.ExportsManagerActor;
import com.skillbill.at.akka.FileSystemWatcherActor;
import com.skillbill.at.guice.GuiceActorUtils;
import com.skillbill.at.guice.GuiceExtension;
import com.skillbill.at.guice.GuiceExtensionImpl;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorSystem;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KileBeatApplication {
	public void run() throws Exception {
		
        //create system
        final ActorSystem system = ActorSystem.create("kile", ConfigFactory.load());
        system.registerExtension(GuiceExtension.provider);

		//Add shutdownhook
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            system.terminate();
        }));

        //configure Guice
        final GuiceExtensionImpl guiceExtension = GuiceExtension.provider.get(system);
        guiceExtension.setInjector(StartSystem.injector);       

        //XXX create before wather because is used internally
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
