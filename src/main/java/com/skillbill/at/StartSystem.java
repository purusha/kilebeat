package com.skillbill.at;

import com.google.inject.Guice;
import com.skillbill.at.guice.AkkaModule;

public class StartSystem {
	public static void main(String[] args) throws Exception {
		
		Guice
			.createInjector(new AkkaModule())
			.getInstance(KileBeatApplication.class)
			.run();
		
    }
}
