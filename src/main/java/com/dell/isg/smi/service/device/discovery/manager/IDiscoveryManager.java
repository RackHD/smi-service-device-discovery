/**
 * Copyright © 2017 DELL Inc. or its subsidiaries.  All Rights Reserved.
 */
package com.dell.isg.smi.service.device.discovery.manager;

import java.util.List;

import com.dell.isg.smi.commons.model.common.DevicesIpsRequest;
import com.dell.isg.smi.commons.model.device.discovery.DiscoverIPRangeDeviceRequests;
import com.dell.isg.smi.commons.model.device.discovery.DiscoverdDeviceResponse;

public interface IDiscoveryManager {

    public List<DiscoverdDeviceResponse> discover(DiscoverIPRangeDeviceRequests discoverIPRangeDeviceRequests) throws Exception;


    public List<DiscoverdDeviceResponse> discover(DevicesIpsRequest deviceIps) throws Exception;

}
