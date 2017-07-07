package com.skillbill.at.kilebeat;

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigResolveOptions;

public class ConfigurationValidatorTest {
	
	@Test
	public void simple() {
		
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("kilebeat.conf").getFile());
		
		if (! file.exists()) {
			throw new IllegalArgumentException("configuration file absent");
		}
		
		Config load = ConfigFactory.parseFile(file);
		load.resolve(ConfigResolveOptions.noSystem());
		//System.out.println(load);
		
		if (load.isEmpty()) {
			throw new IllegalArgumentException("configuration file empty");
		}		
		
//		for (Entry<String, ConfigValue> entry : load.entrySet()) {
//			System.out.println(entry.getKey());
//			System.out.println(entry.getValue());
//			System.out.println("---");
//		}

		List<ConfigObject> objectList = (List<ConfigObject>) load.getObjectList("exports");
		//System.out.println(objectList);
		
		objectList.forEach(e -> {
			System.out.println(e);
			System.out.println(e.get("path").render());			
			System.out.println(e.get("http").render());			
			System.out.println(e.toConfig().getString("http.url"));			
		});
		
//		ConfigObject root = load.root();
//		for (String string : root.keySet()) {
//			System.out.println(string);
//						
//			System.out.println(load.getConfig(string));
//			System.out.println("---");			
//		}
		
//		Map<String, Config> c = root.keySet().stream().collect(Collectors.toMap(k -> k, k -> root.atKey(k)));
//		c.keySet().stream().forEach(k -> {
//			System.out.println(k);
//			System.out.println(c.get(k));
//			System.out.println("---");
//		});
		
		
	}
	
}
