package com.skillbill.at.akka;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;
import com.skillbill.at.akka.dto.WatchResource;
import com.skillbill.at.guice.GuiceAbstractActor;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileSystemWatcherActor extends GuiceAbstractActor {	
	private final WatchService watcher;
	private final Map<WatchKey, WatchResource> keys;
	
	@Inject
	@SuppressWarnings("unchecked")
	public FileSystemWatcherActor(Config config) throws IOException {
		this.watcher = FileSystems.getDefault().newWatchService();
		this.keys = new HashMap<>();
		
		final ActorSystem system = getContext().system();
		
		((List<ConfigObject>) config.getObjectList("exports")).forEach(obj -> {
			final Config c = obj.toConfig();			
			final File resource = new File(c.getString("path"));
			
			if (resource.exists()) {
				LOGGER.info("path {} is a regule file", resource);
				
				system.actorSelection("user/manager").tell(c, ActorRef.noSender());								
			} else {
				LOGGER.info("path {} is a NOT regule file", resource);
								
				getSelf().tell(new WatchResource(resource.getParentFile(), resource.getName()), ActorRef.noSender());
			}
		});						
	}
	
	@Override
	public void postStop() throws Exception {
		super.postStop();
		
		LOGGER.info("end {} ", getSelf().path());
	}
	
	@Override
	public void preStart() throws Exception {
		super.preStart();
		
		LOGGER.info("start {} with parent {}", getSelf().path(), getContext().parent());
	}
	
	@Override
	public Receive createReceive() {
		return receiveBuilder()
			.match(WatchResource.class, wr -> {
				String name = wr.getName();
				LOGGER.info("name {}", name);
				
				File parentFile = wr.getParentFile();
				LOGGER.info("parentFile {}", parentFile);				

				//XXX NPE for parentFile is NULL
				keys.put(
					parentFile.toPath().register(watcher, ENTRY_CREATE, ENTRY_DELETE), wr
				);
			})
			.build();
	}
}
