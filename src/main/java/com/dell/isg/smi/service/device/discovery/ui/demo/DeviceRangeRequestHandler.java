
package com.dell.isg.smi.service.device.discovery.ui.demo;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class DeviceRangeRequestHandler {
	private List<DeviceRangeRequest> rangeSet = new ArrayList<DeviceRangeRequest>();
	
	public List<DeviceRangeRequest> getRangeSet() {
		return rangeSet;
	}

	public void setRangeSet(List<DeviceRangeRequest> rangeSet) {
		this.rangeSet = rangeSet;
	}

	public void addRange(DeviceRangeRequest deviceRangeRequest) {
		rangeSet.add(deviceRangeRequest);
	}

	public void delete(DeviceRangeRequest deviceRangeRequest) {
		rangeSet.remove(deviceRangeRequest);
	}

}
