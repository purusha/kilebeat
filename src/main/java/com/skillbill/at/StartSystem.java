package com.skillbill.at;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.skillbill.at.guice.AkkaModule;

public class StartSystem {
	public static Injector injector;
	
	public static void main(String[] args) throws Exception {
		injector = Guice.createInjector(new AkkaModule());

		injector.getInstance(KileBeatApplication.class).run();
    }
}

/*

	Remove http/kafka types from:
	
	> com.skillbill.at.akka.TailerActor
	> com.skillbill.at.akka.dto.EndPointFailed
	> com.skillbill.at.configuration.ConfigurationValidator.SingleConfiguration
	> com.skillbill.at.configuration.ConfigurationValidator.ValidationResponse
	
	
	
	-----------------------------------------
		
	Remove kilebeat.conf_* from GitHub
	

*/