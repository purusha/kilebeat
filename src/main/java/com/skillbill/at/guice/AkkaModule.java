package com.skillbill.at.guice;

import java.io.File;
import java.util.Map;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.name.Names;
import com.skillbill.at.configuration.ConfigurationValidator;
import com.skillbill.at.configuration.ValidationResponse;

public class AkkaModule implements Module {

	public static final String CONF = "CONF";

	@Override
	public void configure(Binder binder) {

		final String property = System.getProperty("config.file", "kilebeat.conf");
		final File file = new File(property);				
		final ValidationResponse validResp = new ConfigurationValidator(file).isValid();
		
		if (!validResp.isValid()) {
			throw new RuntimeException("config.file is INVALID");
		}
		
		binder
			.bind(Map.class)
			.annotatedWith(Names.named(CONF))
			.toInstance(validResp.getConfig());
		
	}

}
