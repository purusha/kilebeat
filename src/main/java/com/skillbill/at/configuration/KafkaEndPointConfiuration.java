package com.skillbill.at.configuration;

import org.apache.commons.lang3.StringUtils;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class KafkaEndPointConfiuration implements ConfigurationEndpoint {
	private String queue;
	private String url;

	public KafkaEndPointConfiuration(String queue) {
		final String[] split = StringUtils.split(queue, "@");
		
		this.url = split[0];
		this.queue = split[1];
	}	
}
