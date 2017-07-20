package com.skillbill.at.configuration;

import java.util.Collections;
import java.util.List;

public class Configuration {

	private final String path;
	private final List<ConfigurationEdnpoint> endpoints;
	
	public Configuration(String path) {
		this.path = path;
		this.endpoints = Collections.emptyList();
	}
	
}
