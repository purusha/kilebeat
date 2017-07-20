package com.skillbill.at.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.typesafe.config.Config;

public class ValidationResponse {	
	private Map<Integer, Configuration> configs;
	private Map<Integer, List<String>> errors;

	public ValidationResponse() {
		this.configs = new HashMap<>();
		this.errors = new HashMap<>();
	}

	public void addError(int i, String e) {
		if (errors.containsKey(i)) {
			errors.put(i, new ArrayList<>());
		}
		
		errors.get(i).add(e);
	}

	public boolean isValid() {
		return errors.isEmpty();
	}	
	
	public Map<Integer, Configuration> getConfig() {
		return Collections.unmodifiableMap(configs);
	}

	public boolean containsError(int i) {
		return errors.containsKey(i);
	}

	public void addConfiguration(int i, Config eConfig) {
		final Configuration build = new Configuration("");
		
		configs.put(i, build);
	}	
}