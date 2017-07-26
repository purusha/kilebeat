package com.skillbill.at.service;

import java.time.Duration;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.skillbill.at.configuration.ConfigurationEndpoint;
import com.skillbill.at.configuration.HttpEndPointConfiuration;
import com.skillbill.at.configuration.KafkaEndPointConfiuration;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigList;
import com.typesafe.config.ConfigMemorySize;
import com.typesafe.config.ConfigMergeable;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigOrigin;
import com.typesafe.config.ConfigResolveOptions;
import com.typesafe.config.ConfigValue;

import lombok.Getter;

public enum Endpoint {

	HTTP("http", HttpEndPointConfiuration.class),
	KAFKA("kafka", KafkaEndPointConfiuration.class);
	
	@Getter
	private final String confKey;
	
	private final Class<? extends ConfigurationEndpoint> confClazz;

	private Endpoint(String confKey, Class<? extends ConfigurationEndpoint> confClazz) {
		this.confKey = confKey;
		this.confClazz = confClazz;		
	}

	public ConfigurationEndpoint buildEndpoint(Config config) {		
		try {
			return confClazz.getConstructor(Config.class).newInstance(config);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}	
	}

	public static ConfigurationEndpoint buildFake(ConfigurationEndpoint ep) {
		try {			
			Class<?> requested = Class.forName(ep.getClass().getName());
			
			for (Endpoint endpoint : values()) {
				if (requested.equals(endpoint.confClazz)) {
					return endpoint.buildEndpoint(new EmptyConfig());
				}
			}					
		} catch (ClassNotFoundException e) { 
			e.printStackTrace();
		}

		throw new RuntimeException("unknow " + ep.getClass().getName());		
	}	
	
	private static class EmptyConfig implements Config {

		@Override
		public ConfigObject root() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ConfigOrigin origin() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Config withFallback(ConfigMergeable other) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Config resolve() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Config resolve(ConfigResolveOptions options) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean isResolved() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public Config resolveWith(Config source) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Config resolveWith(Config source, ConfigResolveOptions options) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void checkValid(Config reference, String... restrictToPaths) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean hasPath(String path) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean hasPathOrNull(String path) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isEmpty() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public Set<Entry<String, ConfigValue>> entrySet() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean getIsNull(String path) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean getBoolean(String path) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public Number getNumber(String path) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getInt(String path) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public long getLong(String path) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public double getDouble(String path) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public String getString(String path) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <T extends Enum<T>> T getEnum(Class<T> enumClass, String path) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ConfigObject getObject(String path) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Config getConfig(String path) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object getAnyRef(String path) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ConfigValue getValue(String path) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Long getBytes(String path) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ConfigMemorySize getMemorySize(String path) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Long getMilliseconds(String path) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Long getNanoseconds(String path) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getDuration(String path, TimeUnit unit) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Duration getDuration(String path) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ConfigList getList(String path) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<Boolean> getBooleanList(String path) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<Number> getNumberList(String path) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<Integer> getIntList(String path) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<Long> getLongList(String path) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<Double> getDoubleList(String path) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<String> getStringList(String path) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <T extends Enum<T>> List<T> getEnumList(Class<T> enumClass, String path) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<? extends ConfigObject> getObjectList(String path) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<? extends Config> getConfigList(String path) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<? extends Object> getAnyRefList(String path) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<Long> getBytesList(String path) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<ConfigMemorySize> getMemorySizeList(String path) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<Long> getMillisecondsList(String path) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<Long> getNanosecondsList(String path) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<Long> getDurationList(String path, TimeUnit unit) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<Duration> getDurationList(String path) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Config withOnlyPath(String path) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Config withoutPath(String path) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Config atPath(String path) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Config atKey(String key) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Config withValue(String path, ConfigValue value) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
}
