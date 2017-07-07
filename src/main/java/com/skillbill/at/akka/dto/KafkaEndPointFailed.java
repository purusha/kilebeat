package com.skillbill.at.akka.dto;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import lombok.ToString;

@ToString
public class KafkaEndPointFailed {

	private KafkaEndPointConfiuration conf;
	private final LocalDateTime now;

	public KafkaEndPointFailed(KafkaEndPointConfiuration conf) {
		this.conf = conf;
		this.now = LocalDateTime.now();
	}

	public KafkaEndPointConfiuration getConf() {
		return conf;
	}

	public boolean isExpired() {
		return ChronoUnit.SECONDS.between(now, LocalDateTime.now()) > 60;
	}	

}
