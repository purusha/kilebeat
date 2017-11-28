package com.skillbill.at.guice;

import java.io.File;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.skillbill.at.configuration.ConfigurationValidator;
import com.skillbill.at.configuration.ConfigurationValidator.ExportsConfiguration;
import com.skillbill.at.configuration.ConfigurationValidator.ValidationResponse;
import com.skillbill.at.service.FileSystemWatcherService;

public class AkkaModule implements Module {
	@Override
	public void configure(Binder binder) {
		
		final String property = System.getProperty("config.file", "kilebeat.conf");				
		final ValidationResponse validResp = new ConfigurationValidator().isValid(new File(property));		
		
		if (!validResp.isValid()) {
			System.err.println("config.file is INVALID ... exit!!?");
			System.exit(-1);
		}
		
		binder
			.bind(FileSystemWatcherService.class)
			.in(Singleton.class);
		
		binder
			.bind(ExportsConfiguration.class)
			.toInstance(validResp.getConfig());
		
	}
}
