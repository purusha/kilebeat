package com.skillbill.at.service;

import org.apache.commons.lang3.RandomStringUtils;

public class ActorNamesFactory {
	
	public static String tailer(){
		return "tailer" + rand();
	}
	
	private static String rand() {
		return RandomStringUtils.random(4, false, true);
	}

}
