package com.dell.isg.smi.service.device.discovery.ui.demo;

import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class DeviceRangeRequest {

	String id;
	Set<String> deviceGroups;
	@NotNull(message = "startIp is required")
	@Size(min = 3, max = 50, message = "name must be longer than 3 and less than 40 characters")
	String startIp;
	@NotNull(message = "endIp is required")
	@Size(min = 0, max = 500, message = "name must be longer than 3 and less than 500 characters")
	String endIp;
	String username;
	String password;

	public DeviceRangeRequest() {
		super();
	}

	public Set<String> getDeviceGroups() {
		return deviceGroups;
	}

	public void setDeviceGroups(Set<String> deviceGroups) {
		this.deviceGroups = deviceGroups;
	}

	public String getStartIp() {
		return startIp;
	}

	public void setStartIp(String startIp) {
		this.startIp = startIp;
	}

	public String getEndIp() {
		return endIp;
	}

	public void setEndIp(String endIp) {
		this.endIp = endIp;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
