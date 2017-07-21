package com.skillbill.at.akka.dto;

import java.io.File;

import com.skillbill.at.configuration.ConfigurationValidator.SingleConfiguration;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WatchResource {

	private SingleConfiguration conf;
	
	public File parentDirectory() {
		return new File(conf.getPath()).getParentFile();
	}
	
}
