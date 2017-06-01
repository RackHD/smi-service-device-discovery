package com.dell.isg.smi.service.device.discovery.ui.demo;

import java.io.Serializable;

public class DeviceRangeRequestModifiedEvent implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -3802187894585926758L;
	private final DeviceRangeRequest deviceRangeRequest;

    public DeviceRangeRequestModifiedEvent(DeviceRangeRequest deviceRangeRequest) {
        this.deviceRangeRequest = deviceRangeRequest;
    }

    public DeviceRangeRequest getDeviceRangeRequest() {
        return deviceRangeRequest;
    }
    
}
