/**
 * Copyright © 2017 DELL Inc. or its subsidiaries.  All Rights Reserved.
 */
package com.dell.isg.smi.service.device.discovery.config;

import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.dell.isg.smi.service.device.discovery")
public class InnerConfig {

    @Bean
    @RefreshScope
    public DiscoveryDeviceConfig serviceConfig() {
        return new DiscoveryDeviceConfig();
    }
}
