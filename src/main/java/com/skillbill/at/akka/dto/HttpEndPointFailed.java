package com.skillbill.at.akka.dto;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import lombok.ToString;

@ToString
public class HttpEndPointFailed {
	private final HttpEndPointConfiuration conf;
	private final LocalDateTime now; 

	public HttpEndPointFailed(HttpEndPointConfiuration conf) {
		this.conf = conf;
		this.now = LocalDateTime.now();
	}

	public HttpEndPointConfiuration getConf() {
		return conf;
	}

	public boolean isExpired() {
		return ChronoUnit.SECONDS.between(now, LocalDateTime.now()) > 60;
	}
}
