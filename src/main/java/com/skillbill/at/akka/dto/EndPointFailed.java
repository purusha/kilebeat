package com.skillbill.at.akka.dto;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import com.skillbill.at.configuration.ConfigurationEndpoint;
import com.skillbill.at.configuration.HttpEndPointConfiuration;
import com.skillbill.at.configuration.KafkaEndPointConfiuration;

import lombok.ToString;

@ToString
public class EndPointFailed {
	private final ConfigurationEndpoint conf;
	private final LocalDateTime now; 

	public EndPointFailed(ConfigurationEndpoint conf) {
		this.conf = conf;
		this.now = LocalDateTime.now();
	}

	public ConfigurationEndpoint getConf() {
		return conf;
	}

	public boolean isExpired() {
		return ChronoUnit.SECONDS.between(now, LocalDateTime.now()) > 60;
	}
	
	public boolean isHttp() {
		return conf instanceof HttpEndPointConfiuration;
	}
	
	public boolean isKafka() {
		return conf instanceof KafkaEndPointConfiuration;
	}
}
