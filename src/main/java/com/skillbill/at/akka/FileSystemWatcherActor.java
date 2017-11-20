package com.skillbill.at.akka;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import com.google.inject.Inject;
import com.skillbill.at.configuration.ConfigurationValidator.ExportsConfiguration;
import com.skillbill.at.configuration.ConfigurationValidator.SingleConfiguration;
import com.skillbill.at.guice.GuiceAbstractActor;
import com.skillbill.at.service.FileSystemWatcher;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import lombok.extern.slf4j.Slf4j;
import scala.concurrent.duration.FiniteDuration;

@Slf4j
public class FileSystemWatcherActor extends GuiceAbstractActor {	
	private static final String SCHEDULATION_WATCH = "SchedulationsWatch";
		
	private final FileSystemWatcher fsWatcher;
	private final ActorSystem system = getContext().system();
	private Cancellable schedule;
	
	@Inject
	public FileSystemWatcherActor(ExportsConfiguration config, FileSystemWatcher watcher) throws IOException {
		this.fsWatcher = watcher;		
				
		this.schedule = system.scheduler().scheduleOnce(FiniteDuration.create(30, TimeUnit.SECONDS), 
			getSelf(), SCHEDULATION_WATCH, system.dispatcher(), getSelf());
		
		config.getExports().forEach(obj -> {			
			final File resource = obj.getPath();
			
			if (resource.exists()) {
				LOGGER.info("on path {} ... run tail", resource);				
				buildTailerActorFor(obj);								
			} else {
				LOGGER.info("on path {} ... can't run tail", resource);											
				fsWatcher.watch(obj);
			}
		});						
	}
	
	@Override
	public void postStop() throws Exception {
		super.postStop();
		LOGGER.info("end {} ", getSelf().path());
		
		fsWatcher.close();		
		schedule.cancel();
	}
	
	@Override
	public void preStart() throws Exception {
		super.preStart();		
		LOGGER.info("start {} with parent {}", getSelf().path(), getContext().parent().path());
	}
	
	@Override
	public Receive createReceive() {
		return receiveBuilder()
			.matchEquals(SCHEDULATION_WATCH, sw -> {				
				
				LOGGER.info("### check new files");								

				//XXX every WatchKey should poll the same file ... but i can process it only one times 
				final Set<Path> consumedResource = new HashSet<>();
				
				fsWatcher.getKeys().entrySet().forEach(e -> {					
					final WatchKey wKey = e.getValue();
					
					for (WatchEvent<?> we : wKey.pollEvents()) {
						final Optional<SingleConfiguration> canHandle = isRelated(we, e.getKey());
						final Path path = (Path)we.context();
						
						if (canHandle.isPresent() && !consumedResource.contains(path)) {
							LOGGER.info("on path {} ... run tail", canHandle.get().getPath());
							consumedResource.add(path);							
							buildTailerActorFor(canHandle.get());
						} else {
							final String parentPath = e.getKey().getPath().getParent();
							LOGGER.info("on path {} ... can't run tail", parentPath + "/" + path);
						}
					}
				});				
								
				//XXX remove elements from keys Map when file without regExp is viewed
				
				this.schedule = system.scheduler().scheduleOnce(FiniteDuration.create(30, TimeUnit.SECONDS), 
						getSelf(), SCHEDULATION_WATCH, system.dispatcher(), getSelf());
				
			})
			.matchAny(o -> {
				LOGGER.warn("not handled message", o);
				unhandled(o);
			})			
			.build();
	}
	
	private void buildTailerActorFor(SingleConfiguration sc) {
		system
			.actorSelection("user/manager")
			.tell(sc, ActorRef.noSender());
	}

	private Optional<SingleConfiguration> isRelated(WatchEvent<?> we, SingleConfiguration initialConf) {
		final Path context = (Path)we.context();	
		final File initialResource = initialConf.getPath();				
		final String currentName = context.toFile().getName();
		
		final SingleConfiguration newSc;		
		if (match(initialResource.getName(), currentName)) {
			newSc = initialConf.makeCopy(initialResource.getParent() + "/" + currentName);
		} else {
			newSc = null;
		}	
		
		return Optional.ofNullable(newSc);
	}

	//XXX until new idea, we only support '?' and '*' placeholders
	private boolean match(String configName, String fileName) {	
		final String regex = configName.replace("?", ".?").replace("*", ".*?");		
		final Pattern pattern = Pattern.compile(regex);
		
		return pattern.matcher(fileName).matches();
	}	
}