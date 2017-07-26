package com.skillbill.at.integration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.IntStream;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;

public class KafkaIT {
	private static final String TOPIC = "abc42";

	public static void main(String[] args) {
		final KafkaIT kafka = new KafkaIT();
		
		//kafka.produce(100);
		
		kafka.consume(10, 20);
		System.out.println("###################################");
		kafka.consume(10, 120);
	}

	public void consume(int i, int j) {
		final KafkaConsumer<String,String> consumer = new KafkaConsumer<String, String>(configProperties);

		List<PartitionInfo> partitionsFor = consumer.partitionsFor(TOPIC);
		System.out.println(partitionsFor);
		
		Set<TopicPartition> assignment = consumer.assignment();
		System.out.println(assignment);		

		ArrayList<TopicPartition> ass = new ArrayList<>();
		TopicPartition tp = new TopicPartition(TOPIC, partitionsFor.get(0).partition());
		ass.add(tp);		
		consumer.assign(ass);
		
		assignment = consumer.assignment();
		System.out.println(assignment);	
		
		long position = consumer.position(tp);
		System.out.println(tp + " position is " + position);
		
//		consumer.commitSync(
//			Collections.singletonMap(tp, new OffsetAndMetadata(i))
//		);
		
		consumer.seek(tp, i);
		
		final Iterator<ConsumerRecord<String, String>> iterator = consumer.poll(500l).iterator();
		
		while (iterator.hasNext()) {
			ConsumerRecord<String, String> cr = iterator.next();
			long offset = cr.offset();
			
			if (offset > j) {
				break;
			}

			String value = cr.value();
			System.out.println(offset + " => " + value);
		}
		
		consumer.close();
	}

	private final Properties configProperties;
	
	public KafkaIT() {
        configProperties = new Properties();
        configProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"localhost:9092");
        
        configProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.ByteArraySerializer");
        configProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer");
        
        configProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.ByteArrayDeserializer");
        configProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringDeserializer");
        
        configProperties.put("enable.auto.commit", "false");
		
                 
	}

	public void produce(int i) {		
		final KafkaProducer<String, String> producer = new KafkaProducer<String, String>(configProperties);
		
		IntStream.rangeClosed(1, i).forEach(count -> {
			try {
				long offset = producer.send(
					new ProducerRecord<String, String>(TOPIC, RandomStringUtils.randomAscii(250))
				).get().offset();
				
				System.out.println("offset is " + offset);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});		
		
		producer.close();
	}

}
