package com.skillbill.at;

import org.apache.commons.lang3.RandomStringUtils;

public class ActorNamesFactory {
	
	public static String tailer(){
		return "tailer" + RandomStringUtils.random(10, false, true);
	}
	
	public static String http(){
		return "http" + RandomStringUtils.random(10, false, true);
	}

	public static String kafka(){
		return "kafka" + RandomStringUtils.random(10, false, true);
	}

}
