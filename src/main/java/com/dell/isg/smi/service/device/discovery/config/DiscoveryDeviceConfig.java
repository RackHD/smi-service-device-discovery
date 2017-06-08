/**
 * Copyright © 2017 DELL Inc. or its subsidiaries.  All Rights Reserved.
 */
package com.dell.isg.smi.service.device.discovery.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.dell.isg.smi.commons.model.device.discovery.config.DiscoveryConfig;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@ConfigurationProperties
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "discoveryConfig"
})
public class DiscoveryDeviceConfig {
	
	 @JsonProperty("discoveryConfig")
	    private DiscoveryConfig discoveryConfig;

	    /**
	     * No args constructor for use in serialization
	     * 
	     */
	    public DiscoveryDeviceConfig() {
	    }

	    /**
	     * 
	     * @param discoveryConfig
	     */
	    public DiscoveryDeviceConfig(DiscoveryConfig discoveryConfig) {
	        super();
	        this.discoveryConfig = discoveryConfig;
	    }

	    @JsonProperty("discoveryConfig")
	    public DiscoveryConfig getDiscoveryConfig() {
	        return discoveryConfig;
	    }

	    @JsonProperty("discoveryConfig")
	    public void setDiscoveryConfig(DiscoveryConfig discoveryConfig) {
	        this.discoveryConfig = discoveryConfig;
	    }


}
