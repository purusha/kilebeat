package com.skillbill.at.configuration;

import java.io.File;
import java.util.List;
import java.util.stream.IntStream;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConfigurationValidator {	
	private File configuration;

	public ConfigurationValidator(File configuration) {
		this.configuration = configuration;
	}	
	
	public ValidationResponse isValid() {
		if (!configuration.exists()) {
			throw new IllegalArgumentException("configuration file absent");
		}

		final Config load = ConfigFactory.parseFile(configuration);
		
		if (load.isEmpty() || !load.hasPath("exports")) {
			throw new IllegalArgumentException("configuration file not valid");
		}
		
		@SuppressWarnings("unchecked")
		final List<ConfigObject> exports = (List<ConfigObject>) load.getObjectList("exports");

		if (exports.isEmpty()) {
			throw new IllegalArgumentException("configuration file not valid");
		}
		
		final ValidationResponse response = new ValidationResponse();
		
		IntStream.range(0, exports.size())
			.forEachOrdered(i -> {				
				final Config eConfig = exports.get(i).toConfig();
				LOGGER.debug("{}° => {}", i, eConfig);
				
				/*
				 * validation starts
				 */
				
				if (!eConfig.hasPath("path")) {
					response.addError(i, String.format("%d element does not contains %s", i, "path"));
				}
				
				if (!eConfig.hasPath("http") && !eConfig.hasPath("kafka")) {
					response.addError(i, String.format("%d element does not contains both %s and %s", i, "http", "kafka"));
				}

				if (eConfig.hasPath("http")) {
					if (!eConfig.hasPath("http.url")) {
						response.addError(i, String.format("%d element does not contains %s", i, "http.url"));
					}
				}

				if (eConfig.hasPath("kafka")) {
					if (!eConfig.hasPath("kafka.queue")) {
						response.addError(i, String.format("%d element does not contains %s", i, "kafka.queue"));
					}					
				}		
				
				/*
				 * finally add Configuration 
				 */
				
				if (!response.containsError(i)) {
					response.addConfiguration(i, eConfig);
				}
				
			});

//			System.out.println(e.get("path").render());			
//			System.out.println(e.get("http").render());			
//			System.out.println(e.toConfig().getString("http.url"));					
		
		return response;
	}	
}