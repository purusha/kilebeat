package com.skillbill.at.akka;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import com.google.inject.Inject;
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
	
	private final Map<SingleConfiguration, WatchService> watchers;
	private final Map<SingleConfiguration, WatchKey> keys;
	private Cancellable schedule;
	
	@Inject
	public FileSystemWatcherActor(ExportsConfiguration config) throws IOException {
		this.watchers = new HashMap<>();
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
				getSelf().tell(obj, ActorRef.noSender());
			}
		});						
	}
	
	@Override
	public void postStop() throws Exception {
		super.postStop();
		LOGGER.info("end {} ", getSelf().path());
		
		watchers.values().forEach(ws -> {
			try {
				ws.close();
			} catch (IOException e) {
				LOGGER.error("", e);				
			}
		});
		
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
			.match(SingleConfiguration.class, sc -> {				
				final File parentFile = new File(sc.getPath()).getParentFile();
				LOGGER.info("parentFile {} for configuration {}", parentFile, sc);
				
				final Path path = parentFile.toPath();
				final WatchService wService = path.getFileSystem().newWatchService();				

				//XXX NPE for parentFile is NULL				
				keys.put(sc, path.register(wService, ENTRY_CREATE));
				watchers.put(sc, wService);
			})
			.matchEquals(SCHEDULATION_WATCH, sw -> {
				final ActorSystem system = getContext().system();				
				this.schedule = system.scheduler().scheduleOnce(FiniteDuration.create(30, TimeUnit.SECONDS), 
					getSelf(), SCHEDULATION_WATCH, system.dispatcher(), getSelf());
				
				LOGGER.info("### check new files");				
				//LOGGER.info("{}", keys);
				
				keys.keySet().forEach(initialConf -> {
					keys.get(initialConf).pollEvents().stream()
						.map(we -> canHandleEvent(we, initialConf))
						.filter(osc -> osc.isPresent())
						.map(osc -> osc.get())
						.forEach(
							sc -> system.actorSelection("user/manager").tell(sc, ActorRef.noSender())
						);
				});		
				
				//XXX when remove elements from keys Map ???
			})
			.matchAny(o -> {
				LOGGER.warn("not handled message", o);
			})			
			.build();
	}

	private Optional<SingleConfiguration> canHandleEvent(WatchEvent<?> we, SingleConfiguration initialConf) {
		final Path context = (Path)we.context();	
		final File initialResource = new File(initialConf.getPath());				
		final String currentName = context.toFile().getName();
		final String path = initialResource.getParent() + "/" + currentName;
		SingleConfiguration newSc = null;
		
		if (matchConfiguration(initialResource.getName(), currentName)) {
			newSc = initialConf.makeCopy(path);
			LOGGER.info("on path {} ... run tail", newSc.getPath());
		} else {
			LOGGER.info("skip file {} because not match the given pattern {}", path, initialResource.getAbsolutePath());
		}	
		
		return Optional.ofNullable(newSc);
	}

	private boolean matchConfiguration(String initialName, String currentName) {	
		final String regex = initialName.replace("?", ".?").replace("*", ".*?");		
		final Pattern pattern = Pattern.compile(regex);
		
		return pattern.matcher(currentName).matches();
	}	
}