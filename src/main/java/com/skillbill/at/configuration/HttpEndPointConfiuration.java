package com.skillbill.at.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HttpEndPointConfiuration implements ConfigurationEndpoint {

	private String path;

}
