package com.skillbill.at.akka;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import com.google.inject.Inject;
import com.skillbill.at.akka.dto.WatchResource;
import com.skillbill.at.configuration.ConfigurationValidator.ExportsConfiguration;
import com.skillbill.at.configuration.ConfigurationValidator.SingleConfiguration;
import com.skillbill.at.guice.GuiceAbstractActor;

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
	public FileSystemWatcherActor(ExportsConfiguration config) throws IOException {
		this.watcher = FileSystems.getDefault().newWatchService();
		this.keys = new HashMap<>();
		
		final ActorSystem system = getContext().system();		
		this.schedule = system.scheduler().scheduleOnce(FiniteDuration.create(30, TimeUnit.SECONDS), 
			getSelf(), SCHEDULATION_WATCH, system.dispatcher(), getSelf());
		
		config.getExports().forEach(obj -> {			
			final File resource = new File(obj.getPath());
			
			if (resource.exists()) {
				LOGGER.info("on path {} ... run tail", resource);				
				system.actorSelection("user/manager").tell(obj, ActorRef.noSender());								
			} else {
				LOGGER.info("on path {} ... can't run tail", resource);				
				getSelf().tell(new WatchResource(obj), ActorRef.noSender());
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
				
				File parentFile = wr.parentDirectory();
				LOGGER.info("parentFile {}", parentFile);				

				//XXX NPE for parentFile is NULL
				keys.put(
					parentFile.toPath().register(watcher, ENTRY_CREATE), wr
				);
			})
			.matchEquals(SCHEDULATION_WATCH, sw -> {
				final ActorSystem system = getContext().system();				
				this.schedule = system.scheduler().scheduleOnce(FiniteDuration.create(30, TimeUnit.SECONDS), 
					getSelf(), SCHEDULATION_WATCH, system.dispatcher(), getSelf());
				
				LOGGER.info("### check new files");				
				
				keys.keySet().forEach(wk -> {
					final SingleConfiguration initialConf = keys.get(wk).getConf();					
					
					wk.pollEvents().forEach(we -> {						
						final Path context = (Path)we.context();	
						final File initialResource = new File(initialConf.getPath());				
						final String currentName = context.toFile().getName();
						
						if (matchConfiguration(initialResource.getName(), currentName)) {
							final SingleConfiguration newSc = initialConf.makeCopy(initialResource.getParent() + "/" + currentName);
							LOGGER.info("on path {} ... run tail", newSc.getPath());
							
							system.actorSelection("user/manager").tell(newSc, ActorRef.noSender());							
						} else {
							LOGGER.info(
								"skip file {} because not match the given pattern {}", 
								initialResource.getParent() + "/" + currentName, initialResource.getAbsolutePath()
							);
						}						
					});
				});								
			})
			.build();
	}

	private boolean matchConfiguration(String initialName, String currentName) {	
		final String regex = initialName.replace("?", ".?").replace("*", ".*?");		
		final Pattern pattern = Pattern.compile(regex);
		
		return pattern.matcher(currentName).matches();
	}
}