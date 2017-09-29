/**
 * Copyright © 2017 DELL Inc. or its subsidiaries.  All Rights Reserved.
 */
package com.dell.isg.smi.service.device.discovery.controller;

import java.util.List;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.dell.isg.smi.commons.utilities.CustomRecursiveToStringStyle;
import com.dell.isg.smi.commons.model.common.DevicesIpsRequest;
import com.dell.isg.smi.commons.model.device.discovery.DiscoverIPRangeDeviceRequests;
import com.dell.isg.smi.commons.model.device.discovery.DiscoverdDeviceResponse;
import com.dell.isg.smi.service.device.discovery.manager.IDiscoveryManager;
import com.dell.isg.smi.service.device.exception.BadRequestException;
import com.dell.isg.smi.service.device.exception.EnumErrorCode;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/api/1.0/discover")
public class DeviceDiscoveryController {

    @Autowired
    IDiscoveryManager discoveryManager;

    private static final Logger logger = LoggerFactory.getLogger(DeviceDiscoveryController.class.getName());

    @RequestMapping(value = "/range", method = RequestMethod.POST, headers = "Accept=application/json", consumes = "application/json", produces = "application/json")
    @ApiOperation(value = "/range", nickname = "range", notes = "This operation will ping sweep and discover devices within the given IP range.", response = DiscoverdDeviceResponse.class, responseContainer = "List")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Success", response = DiscoverdDeviceResponse.class), @ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 500, message = "Failure") })
    public List<DiscoverdDeviceResponse> discover(@RequestBody DiscoverIPRangeDeviceRequests discoverIPRangeDeviceRequests) throws Exception {
        logger.trace("Range submitted for discovery : {} ", ReflectionToStringBuilder.toString(discoverIPRangeDeviceRequests, new CustomRecursiveToStringStyle(99)));
        List<DiscoverdDeviceResponse> response = null;
        if (discoverIPRangeDeviceRequests == null) {
            BadRequestException badRequestException = new BadRequestException();
            badRequestException.setErrorCode(EnumErrorCode.ENUM_INVALIDINPUT_ERROR);
            throw badRequestException;
        }
        response = discoveryManager.discover(discoverIPRangeDeviceRequests);
        logger.trace("Discovery Response : {} ", ReflectionToStringBuilder.toString(response, new CustomRecursiveToStringStyle(99)));
        return response;
    }


    @RequestMapping(value = "/ips", method = RequestMethod.POST, headers = "Accept=application/json", consumes = "application/json", produces = "application/json")
    @ApiOperation(value = "/ips", nickname = "ips", notes = "This operation will ping sweep and discover devices within the given IP list.", response = DiscoverdDeviceResponse.class, responseContainer = "List")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Success", response = DiscoverdDeviceResponse.class), @ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 500, message = "Failure") })
    public List<DiscoverdDeviceResponse> discover(@RequestBody DevicesIpsRequest deviceIps)  throws Exception {
        List<DiscoverdDeviceResponse> response = null;
        logger.trace("Ips submitted for discovery : {} ", ReflectionToStringBuilder.toString(deviceIps, new CustomRecursiveToStringStyle(99)));
        if (deviceIps == null) {
            BadRequestException badRequestException = new BadRequestException();
            badRequestException.setErrorCode(EnumErrorCode.ENUM_INVALIDINPUT_ERROR);
            throw badRequestException;
        }
        response = discoveryManager.discover(deviceIps);
        logger.trace("Discovery Response : {} ", ReflectionToStringBuilder.toString(response, new CustomRecursiveToStringStyle(99)));
        return response;
    }

}
