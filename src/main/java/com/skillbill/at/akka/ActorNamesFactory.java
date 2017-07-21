package com.skillbill.at.akka;

import org.apache.commons.lang3.RandomStringUtils;

public class ActorNamesFactory {
	
	public static String tailer(){
		return "tailer" + rand();
	}
	
	public static String http(){
		return "http" + rand();
	}

	public static String kafka(){
		return "kafka" + rand();
	}
		
	private static String rand() {
		return RandomStringUtils.random(4, false, true);
	}

}
