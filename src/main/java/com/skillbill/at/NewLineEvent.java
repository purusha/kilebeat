package com.skillbill.at;

import java.nio.file.Path;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class NewLineEvent {

	private String line;
	private long ts;
	private Path path;

	public NewLineEvent(String line, Path path) {
		this.line = line;		
		this.path = path;
		this.ts = System.currentTimeMillis();
	}
	
}
