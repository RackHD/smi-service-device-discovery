/**
 * Copyright © 2017 DELL Inc. or its subsidiaries.  All Rights Reserved.
 */
package com.dell.isg.smi.service.device.discovery.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.dell.isg.smi.commons.model.device.discovery.DiscoveryDeviceTypeEnum;
import com.dell.isg.smi.service.device.discovery.config.DiscoveryDeviceConfig;
import com.dell.isg.smi.service.device.discovery.config.DiscoveryDeviceConfigProvider;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/api/1.0/discover/config")
public class DeviceConfigController {

    @Autowired
    DiscoveryDeviceConfigProvider discoveryDeviceConfigUtil;


    @RequestMapping(value = "/device", method = RequestMethod.GET, produces = "application/json")
    @ApiOperation(value = "/device", nickname = "device", notes = "This operation allow user to get all service uri from the gateway.", response = DiscoveryDeviceConfig.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Success", response = Object.class), @ApiResponse(code = 401, message = "Unauthorized"), @ApiResponse(code = 403, message = "Forbidden"), @ApiResponse(code = 404, message = "Not Found"), @ApiResponse(code = 500, message = "Failure") })
    public Object discoveryDeviceConfig() {
        return discoveryDeviceConfigUtil.getDiscoveryDeviceTypeByType(DiscoveryDeviceTypeEnum.BROCADE);
    }

}
