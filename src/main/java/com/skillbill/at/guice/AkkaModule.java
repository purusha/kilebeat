package com.skillbill.at.guice;

import java.io.File;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.skillbill.at.configuration.ConfigurationValidator;
import com.skillbill.at.configuration.ConfigurationValidator.ExportsConfiguration;
import com.skillbill.at.configuration.ConfigurationValidator.ValidationResponse;

public class AkkaModule implements Module {
	@Override
	public void configure(Binder binder) {

		final String property = System.getProperty("config.file", "kilebeat.conf");
		final File file = new File(property);				
		final ValidationResponse validResp = new ConfigurationValidator(file).isValid();
		
		if (!validResp.isValid()) {
			throw new RuntimeException("config.file is INVALID");
		}
		
		binder
			.bind(ExportsConfiguration.class)
			.toInstance(validResp.getConfig());			
		
	}
}
