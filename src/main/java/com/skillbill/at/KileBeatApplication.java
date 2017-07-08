package com.skillbill.at;

import com.google.inject.Injector;
import com.skillbill.at.akka.ExportsManagerActor;
import com.skillbill.at.guice.GuiceActorUtils;
import com.skillbill.at.guice.GuiceExtension;
import com.skillbill.at.guice.GuiceExtensionImpl;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorSystem;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KileBeatApplication {
	public void run() throws Exception {
		
		final Injector injector = StartSystem.injector;

        //create system
        final ActorSystem system = ActorSystem.create("kile", ConfigFactory.load());
        system.registerExtension(GuiceExtension.provider);

		//Add shutdownhook
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            system.terminate();
        }));

        //configure Guice
        final GuiceExtensionImpl guiceExtension = GuiceExtension.provider.get(system);
        guiceExtension.setInjector(injector);

        //final Config engineConf = injector.getInstance(Config.class);        

        //clusterSettings
        //final ClusterSingletonManagerSettings settings = ClusterSingletonManagerSettings.create(system).withRole("master");

//	        //create instance of Singleton Actor for CLUSTER
//	        system.actorOf(
//	    		ClusterSingletonManager.props(
//					GuiceActorUtils.makeProps(system, SchedulerActor.class), PoisonPill.class, settings), "scheduler-actor"
//			);
        
        system.actorOf(
    		GuiceActorUtils.makeProps(system, ExportsManagerActor.class)
		);
        
        LOGGER.info("-------------------------------------------------");
        LOGGER.info(" KileBeat STARTED");
        LOGGER.info("-------------------------------------------------");

    }
}
