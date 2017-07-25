package com.skillbill.at.kilebeat;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

public class WatcherIT {
	public static void main(String[] args) throws Exception {
		
		final File parentFile = new File("/Users/power/Tmp");
		final WatchService wService = FileSystems.getDefault().newWatchService();
		
		{		
			WatchKey register = parentFile.toPath().register(wService, ENTRY_CREATE);
			System.err.println(register);
		}
	
		{
			WatchKey register = parentFile.toPath().register(wService, ENTRY_CREATE);
			System.err.println(register);
		}
				
		wService.close();
	}
}
