package com.skillbill.at.configuration;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.apache.commons.beanutils.BeanUtils;

import com.skillbill.at.service.Endpoint;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigList;
import com.typesafe.config.ConfigMemorySize;
import com.typesafe.config.ConfigMergeable;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigOrigin;
import com.typesafe.config.ConfigResolveOptions;
import com.typesafe.config.ConfigValue;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ConfigurationValidator {	
	public ValidationResponse isValid(File configuration) {
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
	
	public final class ValidationResponse {	
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
			
			Arrays.stream(Endpoint.values()).forEach(e -> {				
				final Config config = c.hasPath(e.getConfKey()) ? c.getObject(e.getConfKey()).toConfig() : null;
				
				if (config != null && !config.isEmpty()) {
					build.addEndpoint(e.buildEndpoint(config));
				}
			});
						
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
	
	public final class ExportsConfiguration {
		private final SortedMap<Integer, SingleConfiguration> exports;
		
		private ExportsConfiguration(SortedMap<Integer, SingleConfiguration> e) {
			this.exports = e;
		}	
		
		//return in order by key !!?
		public Collection<SingleConfiguration> getExports() { 			
			return exports.values();
		}
	}	
	
	@ToString
	public final class SingleConfiguration {		

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
				final ConfigurationEndpoint dest = buildFakeEndpoint(ep);
				
				try {
					BeanUtils.copyProperties(dest, ep);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				ret.addEndpoint(dest);
			});
			
			return ret;			
		}
		
		//XXX bad code ... please try another manner to do the same work!!?
		private ConfigurationEndpoint buildFakeEndpoint(ConfigurationEndpoint ep) {
			return Endpoint.valueOf(ep).buildEndpoint(new EmptyConfig());
		}		
	}	
	
	private class EmptyConfig implements Config {
		@Override
		public ConfigObject root() {
			return null;
		}

		@Override
		public ConfigOrigin origin() {
			return null;
		}

		@Override
		public Config withFallback(ConfigMergeable other) {
			return null;
		}

		@Override
		public Config resolve() {
			return null;
		}

		@Override
		public Config resolve(ConfigResolveOptions options) {
			return null;
		}

		@Override
		public boolean isResolved() {
			return false;
		}

		@Override
		public Config resolveWith(Config source) {
			return null;
		}

		@Override
		public Config resolveWith(Config source, ConfigResolveOptions options) {
			return null;
		}

		@Override
		public void checkValid(Config reference, String... restrictToPaths) {
		}

		@Override
		public boolean hasPath(String path) {
			return false;
		}

		@Override
		public boolean hasPathOrNull(String path) {
			return false;
		}

		@Override
		public boolean isEmpty() {
			return false;
		}

		@Override
		public Set<Entry<String, ConfigValue>> entrySet() {
			return null;
		}

		@Override
		public boolean getIsNull(String path) {
			return false;
		}

		@Override
		public boolean getBoolean(String path) {
			return false;
		}

		@Override
		public Number getNumber(String path) {
			return null;
		}

		@Override
		public int getInt(String path) {
			return 0;
		}

		@Override
		public long getLong(String path) {
			return 0;
		}

		@Override
		public double getDouble(String path) {
			return 0;
		}

		@Override
		public String getString(String path) {
			return null;
		}

		@Override
		public <T extends Enum<T>> T getEnum(Class<T> enumClass, String path) {
			return null;
		}

		@Override
		public ConfigObject getObject(String path) {
			return null;
		}

		@Override
		public Config getConfig(String path) {
			return null;
		}

		@Override
		public Object getAnyRef(String path) {
			return null;
		}

		@Override
		public ConfigValue getValue(String path) {
			return null;
		}

		@Override
		public Long getBytes(String path) {
			return null;
		}

		@Override
		public ConfigMemorySize getMemorySize(String path) {
			return null;
		}

		@Override
		public Long getMilliseconds(String path) {
			return null;
		}

		@Override
		public Long getNanoseconds(String path) {
			return null;
		}

		@Override
		public long getDuration(String path, TimeUnit unit) {
			return 0;
		}

		@Override
		public Duration getDuration(String path) {
			return null;
		}

		@Override
		public ConfigList getList(String path) {
			return null;
		}

		@Override
		public List<Boolean> getBooleanList(String path) {
			return null;
		}

		@Override
		public List<Number> getNumberList(String path) {
			return null;
		}

		@Override
		public List<Integer> getIntList(String path) {
			return null;
		}

		@Override
		public List<Long> getLongList(String path) {
			return null;
		}

		@Override
		public List<Double> getDoubleList(String path) {
			return null;
		}

		@Override
		public List<String> getStringList(String path) {
			return null;
		}

		@Override
		public <T extends Enum<T>> List<T> getEnumList(Class<T> enumClass, String path) {
			return null;
		}

		@Override
		public List<? extends ConfigObject> getObjectList(String path) {
			return null;
		}

		@Override
		public List<? extends Config> getConfigList(String path) {
			return null;
		}

		@Override
		public List<? extends Object> getAnyRefList(String path) {
			return null;
		}

		@Override
		public List<Long> getBytesList(String path) {
			return null;
		}

		@Override
		public List<ConfigMemorySize> getMemorySizeList(String path) {
			return null;
		}

		@Override
		public List<Long> getMillisecondsList(String path) {
			return null;
		}

		@Override
		public List<Long> getNanosecondsList(String path) {
			return null;
		}

		@Override
		public List<Long> getDurationList(String path, TimeUnit unit) {
			return null;
		}

		@Override
		public List<Duration> getDurationList(String path) {
			return null;
		}

		@Override
		public Config withOnlyPath(String path) {
			return null;
		}

		@Override
		public Config withoutPath(String path) {
			return null;
		}

		@Override
		public Config atPath(String path) {
			return null;
		}

		@Override
		public Config atKey(String key) {
			return null;
		}

		@Override
		public Config withValue(String path, ConfigValue value) {
			return null;
		}		
	}
	
				
}
