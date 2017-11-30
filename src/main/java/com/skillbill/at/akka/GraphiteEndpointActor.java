package com.skillbill.at.akka;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.skillbill.at.akka.dto.EndPointFailed;
import com.skillbill.at.akka.dto.NewLineEvent;
import com.skillbill.at.configuration.GraphiteEndPointConfiuration;
import com.skillbill.at.guice.GuiceAbstractActor;

import akka.actor.ActorRef;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GraphiteEndpointActor extends GuiceAbstractActor {

	private final ObjectMapper om;
	private final GraphiteEndPointConfiuration conf;
	private final Socket socket;
	private final Writer writer;
	
	@Inject
	public GraphiteEndpointActor(GraphiteEndPointConfiuration conf) throws Exception {
		this.conf = conf;
		this.om = new ObjectMapper();
		this.socket = new Socket("localhost", 2003);
		this.writer = new OutputStreamWriter(socket.getOutputStream());
	}
	
	@Override
	public void postStop() throws Exception {
		super.postStop();		
		LOGGER.info("end {} ", getSelf().path());
		
		IOUtils.closeQuietly(socket);
		IOUtils.closeQuietly(writer);
		
		getContext().parent().tell(new EndPointFailed(conf), ActorRef.noSender());
	}
	
	@Override
	public void preStart() throws Exception {
		super.preStart();		
		LOGGER.info("start {} with parent {}", getSelf().path(), getContext().parent().path());
	}
	
	@Override
	public Receive createReceive() {
		return receiveBuilder()
			.match(NewLineEvent.class, s -> send(s))
			.matchAny(o -> {
				LOGGER.warn("not handled message", o);
				unhandled(o);
			})			
			.build();
	}

	private Object send(NewLineEvent s) {
		try {			
			writer.write(serializeValue(s));
			writer.flush();
		} catch (final Exception e) {
			LOGGER.error("", e);
		}		
		
		return null;
	}

	private String serializeValue(NewLineEvent s) throws Exception {		
		final String prefixedName = "boooo"; //?
		final String graphiteName = prefixedName.replaceAll(" ", "_");
		
		return graphiteName + " " + om.writeValueAsString(s) + "\n";	
	}
}
