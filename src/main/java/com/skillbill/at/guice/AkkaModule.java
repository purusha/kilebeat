package com.skillbill.at.guice;

import java.io.File;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.skillbill.at.ConfigurationValidator;
import com.skillbill.at.ValidationResponse;
import com.typesafe.config.Config;

public class AkkaModule implements Module {

	@Override
	public void configure(Binder binder) {

		final String property = System.getProperty("config.file");
		final File file = new File(property);
				
		final ValidationResponse validResp = new ConfigurationValidator(file).isValid();
		if (validResp.isValid()) {
			throw new RuntimeException("config.file is INVALID");
		}
		
		binder
			.bind(Config.class)
			.toInstance(validResp.getConfig());
		
	}

}
