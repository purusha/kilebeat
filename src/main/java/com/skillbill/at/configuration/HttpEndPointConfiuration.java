package com.skillbill.at.configuration;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
public class HttpEndPointConfiuration implements ConfigurationEndpoint {

	private String path;

}
