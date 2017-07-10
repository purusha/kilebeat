package com.skillbill.at.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.typesafe.config.Config;

public class ValidationResponse {
	private Config config;
	private Map<Integer, List<String>> errors;

	public ValidationResponse(Config load) {
		this.config = load;
		this.errors = new HashMap<>();
	}

	public void addError(int i, String e) {
		if (errors.containsKey(i)) {
			errors.put(i, new ArrayList<>());
		}
		
		final List<String> list = errors.get(i);
		list.add(e);
	}

	public boolean isValid() {
		return errors.isEmpty();
	}	
	
	public Config getConfig() {
		return config;
	}	
}