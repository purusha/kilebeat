package com.skillbill.at.akka.dto;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import lombok.ToString;

@ToString
public class EndPointFailed {
	private final Object conf;
	private final LocalDateTime now; 

	public EndPointFailed(Object conf) {
		this.conf = conf;
		this.now = LocalDateTime.now();
	}

	public Object getConf() {
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
