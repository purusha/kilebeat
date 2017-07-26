package com.skillbill.at.akka.dto;

import java.nio.file.Path;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class NewLineEvent {
	private final String line;
	private final long ts;
	private final String path;

	public NewLineEvent(String line, Path path) {
		this.line = line;		
		this.path = path.toString();
		this.ts = System.currentTimeMillis();
	}	
}
