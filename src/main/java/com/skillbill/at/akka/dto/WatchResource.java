package com.skillbill.at.akka.dto;

import java.io.File;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WatchResource {

	private File parentFile;
	private String name;
	
}
