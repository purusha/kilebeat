package com.skillbill.at.akka.dto;

import org.apache.commons.lang3.StringUtils;

public class KafkaEndPointConfiuration {
	private String queue;
	private String url;

	public KafkaEndPointConfiuration(String queue) {
		final String[] split = StringUtils.split(queue, "@");
		
		this.url = split[0];
		this.queue = split[1];
	}
	
	public String getUrl() {
		return url;
	}
	
	public String getQueue() {
		return queue;
	}
}
