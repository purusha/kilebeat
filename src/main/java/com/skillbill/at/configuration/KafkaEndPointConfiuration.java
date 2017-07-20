package com.skillbill.at.configuration;

import org.apache.commons.lang3.StringUtils;

public class KafkaEndPointConfiuration implements ConfigurationEndpoint {
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
