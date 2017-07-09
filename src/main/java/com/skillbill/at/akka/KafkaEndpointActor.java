package com.skillbill.at.akka;

import java.util.Properties;
import java.util.concurrent.Callable;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.skillbill.at.akka.dto.KafkaEndPointConfiuration;
import com.skillbill.at.akka.dto.KafkaEndPointFailed;
import com.skillbill.at.akka.dto.NewLineEvent;
import com.skillbill.at.guice.GuiceAbstractActor;
import com.skillbill.at.http.RetryCommand;

import akka.actor.ActorRef;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KafkaEndpointActor extends GuiceAbstractActor {
	
	private final Producer<String, String> producer;
	private final ObjectMapper om;
	private KafkaEndPointConfiuration conf;
	
	@Inject
	public KafkaEndpointActor() {				
        final Properties configProperties = new Properties();
        configProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"localhost:9092");
        configProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.ByteArraySerializer");
        configProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer");
		
        //inject me please !!?
        producer = new KafkaProducer<String, String>(configProperties);
        
        om = new ObjectMapper();        
	}
	
	@Override
	public Receive createReceive() {
		return receiveBuilder()
			.match(KafkaEndPointConfiuration.class, c -> conf = c)
			.match(NewLineEvent.class, s -> send(s))
			.build();
	}
	
	@Override
	public void postStop() throws Exception {
		super.postStop();
		
		LOGGER.info("end {} ", getSelf().path());
		
		producer.close();
		
		getContext().parent().tell(new KafkaEndPointFailed(conf), ActorRef.noSender());		
	}
	
	@Override
	public void preStart() throws Exception {
		super.preStart();
		
		LOGGER.info("start {} with parent {}", getSelf().path(), getContext().parent());
	}

	private void send(NewLineEvent s) {
		LOGGER.info("[row@{}] {}", getSelf().path(), s);
				
		new RetryCommand(3, s.getPath()).run(new Callable<Void>() {						
			@Override
			public Void call() throws Exception {
				try {
					long offset = producer.send(
						new ProducerRecord<String, String>(conf.getQueue(), om.writeValueAsString(s))
					).get().offset();
								
					LOGGER.info("offset is {}", offset);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}

				return null;
			}		
		});										
	}		
}	
