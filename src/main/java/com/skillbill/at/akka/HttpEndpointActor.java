package com.skillbill.at.akka;

import org.codehaus.jackson.jaxrs.JacksonJsonProvider;

import com.google.inject.Inject;
import com.skillbill.at.NewLineEvent;
import com.skillbill.at.guice.GuiceAbstractActor;
import com.skillbill.at.http.HttpRetryCommand;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpEndpointActor extends GuiceAbstractActor {
	
	private final Client client;
	private HttpEndPointConfiuration conf;
	
	@Inject
	public HttpEndpointActor() {		
		
        final ClientConfig cc = new DefaultClientConfig();  
        cc.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        cc.getSingletons().add(new JacksonJsonProvider());
        
        //inject me please !!?
        client = Client.create(cc);				
	}
	
	@Override
	public Receive createReceive() {
		return receiveBuilder()
			.match(HttpEndPointConfiuration.class, c -> conf = c)
			.match(NewLineEvent.class, s -> send(s))
			.build();
	}
	
	@Override
	public void postStop() throws Exception {
		super.postStop();
		
		LOGGER.info("############################################ " + getSelf().path());
	}
	
	@Override
	public void preStart() throws Exception {
		super.preStart();
		
		LOGGER.info("**************************************** " + getSelf().path());
	}

	private ClientResponse send(NewLineEvent s) {
		//LOGGER.info("[row@{}] {}", getSelf().path(), s);	
		
		return new HttpRetryCommand(3, s.getPath()).run(() -> {			
			final WebResource resource = client.resource(conf.getPath());
			
			final ClientResponse response = resource
				.accept("application/json")
				.type("application/json")
				.post(ClientResponse.class, s);
			
			if (response.getStatus() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());			
			}
			
			return response;			
		});						
	}	
}

@Data
@AllArgsConstructor
class HttpEndPointConfiuration {

	private String path;

}	
