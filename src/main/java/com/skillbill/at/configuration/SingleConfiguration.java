package com.skillbill.at.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SingleConfiguration {
	private final String path;
	private final List<ConfigurationEndpoint> endpoints;
	
	public SingleConfiguration(String path) {
		this.path = path;
		this.endpoints = new ArrayList<>();
	}
	
	public String getPath() {
		return path;
	}

	public void addEndpoint(ConfigurationEndpoint endpoint) {
		endpoints.add(endpoint);
	}
	
	public List<ConfigurationEndpoint> getEndpoints() {
		return Collections.unmodifiableList(endpoints);
	}
}
