package com.skillbill.at.configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.IntStream;

import org.apache.commons.beanutils.BeanUtils;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;

import lombok.Getter;
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
		
		return response;
	}
	
	public class ValidationResponse {	
		private final Map<Integer, SingleConfiguration> configs;
		private final Map<Integer, List<String>> errors;

		private ValidationResponse() {
			this.configs = new HashMap<>();
			this.errors = new HashMap<>();
		}

		private void addError(int i, String e) {
			if (errors.containsKey(i)) {
				errors.put(i, new ArrayList<>());
			}
			
			errors.get(i).add(e);
		}

		private void addConfiguration(int i, Config c) {
			final SingleConfiguration build = new SingleConfiguration(c.getString("path"));
			
			final Config httpConfig = c.hasPath("http") ? c.getObject("http").toConfig() : null;								
			if (httpConfig != null && !httpConfig.isEmpty()) {					
				build.addEndpoint(new HttpEndPointConfiuration(httpConfig.getString("url")));
			}
			
			final Config kafkaConfig = c.hasPath("kafka") ? c.getObject("kafka").toConfig() : null;
			if (kafkaConfig != null && !kafkaConfig.isEmpty()) {
				build.addEndpoint(new KafkaEndPointConfiuration(kafkaConfig.getString("queue")));
			}					
			
			configs.put(i, build);
		}	

		public boolean isValid() {
			return errors.isEmpty();
		}	
		
		public ExportsConfiguration getConfig() {
			return new ExportsConfiguration(
				new TreeMap<Integer, SingleConfiguration>(configs)
			);
		}

		private boolean containsError(int i) {
			return errors.containsKey(i);
		}
	}	
	
	public class ExportsConfiguration {
		private final SortedMap<Integer, SingleConfiguration> exports;
		
		private ExportsConfiguration(SortedMap<Integer, SingleConfiguration> e) {
			this.exports = e;
		}	
		
		//return in order by key !!?
		public Collection<SingleConfiguration> getExports() { 			
			return exports.values();
			//return exports.entrySet().stream().map(e -> exports.get(e)).collect(Collectors.toList());
		}
	}	
	
	public class SingleConfiguration {		
		@Getter
		private final String path;
		
		private final List<ConfigurationEndpoint> endpoints;
		
		private SingleConfiguration(String path) {
			this.path = path;
			this.endpoints = new ArrayList<>();
		}
		
		private void addEndpoint(ConfigurationEndpoint endpoint) {
			endpoints.add(endpoint);
		}
		
		public List<ConfigurationEndpoint> getEndpoints() {
			return Collections.unmodifiableList(endpoints);
		}

		public SingleConfiguration makeCopy(String path) {
			final SingleConfiguration ret = new SingleConfiguration(path);
			
			endpoints.forEach(ep -> {				
				final ConfigurationEndpoint dest = emptyDto(ep);
				
				try {
					BeanUtils.copyProperties(dest, ep);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				ret.addEndpoint(dest);
			});
			
			return ret;			
		}

		private ConfigurationEndpoint emptyDto(ConfigurationEndpoint ep) {
			if (ep instanceof HttpEndPointConfiuration) {
				return new HttpEndPointConfiuration(null);
			} else if (ep instanceof KafkaEndPointConfiuration) {
				return new KafkaEndPointConfiuration(null);
			} else {
				throw new RuntimeException("XXX");
			}
		}
	}	
				
}
