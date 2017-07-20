package com.skillbill.at;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.skillbill.at.guice.AkkaModule;

public class StartSystem {
	public static Injector injector;
	
	public static void main(String[] args) throws Exception {
		injector = Guice.createInjector(new AkkaModule());

		injector.getInstance(KileBeatApplication.class).run();
    }
}
