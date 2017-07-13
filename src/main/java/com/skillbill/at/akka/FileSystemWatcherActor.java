package com.skillbill.at.akka;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.inject.Inject;
import com.skillbill.at.akka.dto.WatchResource;
import com.skillbill.at.guice.GuiceAbstractActor;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import lombok.extern.slf4j.Slf4j;
import scala.concurrent.duration.FiniteDuration;

@Slf4j
public class FileSystemWatcherActor extends GuiceAbstractActor {	
	private static final String SCHEDULATION_WATCH = "SchedulationsWatch";
	
	private final WatchService watcher;
	private final Map<WatchKey, WatchResource> keys;
	private Cancellable schedule;
	
	@Inject
	@SuppressWarnings("unchecked")
	public FileSystemWatcherActor(Config config) throws IOException {
		this.watcher = FileSystems.getDefault().newWatchService();
		this.keys = new HashMap<>();
		
		final ActorSystem system = getContext().system();
		
		this.schedule = system.scheduler().scheduleOnce(
			FiniteDuration.create(10, TimeUnit.SECONDS), 
			getSelf(), SCHEDULATION_WATCH, 
			system.dispatcher(), getSelf());
		
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
		
		watcher.close();
		schedule.cancel();
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
			.matchEquals(SCHEDULATION_WATCH, sw -> {
				LOGGER.info("### check new files");
				final ActorSystem system = getContext().system();
				
				keys.keySet().forEach(wk -> {
					final List<WatchEvent<?>> pollEvents = wk.pollEvents();
					
					pollEvents.forEach(we -> {
						Kind<?> kind = we.kind();
						Object context = we.context();
					});
				});
				
				this.schedule = system.scheduler().scheduleOnce(
					FiniteDuration.create(10, TimeUnit.SECONDS), 
					getSelf(), SCHEDULATION_WATCH, 
					system.dispatcher(), getSelf());
				
			})
			.build();
	}
}
