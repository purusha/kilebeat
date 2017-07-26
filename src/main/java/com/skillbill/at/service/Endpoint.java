package com.skillbill.at.service;

import org.apache.commons.lang3.RandomStringUtils;

import com.skillbill.at.akka.HttpEndpointActor;
import com.skillbill.at.akka.KafkaEndpointActor;
import com.skillbill.at.configuration.ConfigurationEndpoint;
import com.skillbill.at.configuration.HttpEndPointConfiuration;
import com.skillbill.at.configuration.KafkaEndPointConfiuration;
import com.typesafe.config.Config;

import akka.actor.AbstractActor;
import lombok.Getter;

public enum Endpoint {

	HTTP("http", HttpEndPointConfiuration.class, HttpEndpointActor.class),
	KAFKA("kafka", KafkaEndPointConfiuration.class, KafkaEndpointActor.class);
	
	@Getter
	private final String confKey;
	
	private final Class<? extends ConfigurationEndpoint> confClazz;
	
	@Getter
	private final Class<? extends AbstractActor> actorClazz; 

	private Endpoint(String confKey, Class<? extends ConfigurationEndpoint> confClazz, Class<? extends AbstractActor> actorClazz) {
		this.confKey = confKey;
		this.confClazz = confClazz;	
		this.actorClazz = actorClazz;
	}

	public ConfigurationEndpoint buildEndpoint(Config config) {		
		try {
			return confClazz.getConstructor(Config.class).newInstance(config);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}	
	}
	
	public String actorName() {
		return confKey + RandomStringUtils.random(4, false, true);
	}
	
	public static Endpoint valueOf(ConfigurationEndpoint ep) {
		try {			
			final Class<?> requested = Class.forName(ep.getClass().getName());
			
			for (Endpoint endpoint : values()) {
				if (requested.equals(endpoint.confClazz)) {
					return endpoint;
				}
			}					
		} catch (ClassNotFoundException e) { 
			e.printStackTrace();
		}
	
		throw new RuntimeException("unknow " + ep.getClass().getName());				
	}	
}
