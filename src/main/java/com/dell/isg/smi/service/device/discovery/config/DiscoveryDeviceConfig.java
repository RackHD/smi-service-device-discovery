/**
 * Copyright © 2017 DELL Inc. or its subsidiaries.  All Rights Reserved.
 */
package com.dell.isg.smi.service.device.discovery.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.dell.isg.smi.commons.model.device.discovery.config.DiscoveryConfig;

@ConfigurationProperties(prefix = "discoveryConfig", ignoreUnknownFields = false)
public class DiscoveryDeviceConfig extends DiscoveryConfig {

}
