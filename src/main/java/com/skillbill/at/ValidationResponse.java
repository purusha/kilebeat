package com.skillbill.at;

import com.typesafe.config.Config;

public class ValidationResponse {

	private Config config;

	public ValidationResponse(Config load) {
		this.config = load;
	}

	public void addError(int i, String format) {
	}

	public boolean isValid() {
		return false;
	}	
	
	public Config getConfig() {
		return config;
	}
	
}