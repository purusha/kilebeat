package com.skillbill.at.akka.dto;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import lombok.ToString;

@ToString
public class KafkaEndPointFailed implements ConfigurationFailed {
	private KafkaEndPointConfiuration conf;
	private final LocalDateTime now;

	public KafkaEndPointFailed(KafkaEndPointConfiuration conf) {
		this.conf = conf;
		this.now = LocalDateTime.now();
	}

	@Override
	public KafkaEndPointConfiuration getConf() {
		return conf;
	}

	@Override
	public boolean isExpired() {
		return ChronoUnit.SECONDS.between(now, LocalDateTime.now()) > 60;
	}	
}
