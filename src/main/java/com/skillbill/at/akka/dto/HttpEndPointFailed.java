package com.skillbill.at.akka.dto;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import lombok.ToString;

@ToString
public class HttpEndPointFailed implements ConfigurationFailed {
	private final HttpEndPointConfiuration conf;
	private final LocalDateTime now; 

	public HttpEndPointFailed(HttpEndPointConfiuration conf) {
		this.conf = conf;
		this.now = LocalDateTime.now();
	}

	@Override
	public HttpEndPointConfiuration getConf() {
		return conf;
	}

	@Override
	public boolean isExpired() {
		return ChronoUnit.SECONDS.between(now, LocalDateTime.now()) > 60;
	}
}
