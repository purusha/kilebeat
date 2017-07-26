package com.skillbill.at.configuration;

import org.apache.commons.lang3.StringUtils;

import com.typesafe.config.Config;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class KafkaEndPointConfiuration implements ConfigurationEndpoint {
	private String queue;
	private String url;
	
	public KafkaEndPointConfiuration(Config config) {
		final String[] split = StringUtils.split(config.getString("queue"), "@");
		
		this.url = split[0];
		this.queue = split[1];		
	}
}
