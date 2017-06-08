package com.dell.isg.smi.service.device.discovery.ui.config;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class ConfigDeviceType {

	@NotNull(message = "Group is required")
	@Size(min = 3, max = 50, message = "name must be longer than 3 and less than 40 characters")
	String group;
	@NotNull(message = "Name is required")
	@Size(min = 3, max = 50, message = "name must be longer than 3 and less than 40 characters")
	String name;
	@NotNull(message = "IndentifyBy is required")
	@Size(min = 3, max = 50, message = "name must be longer than 3 and less than 40 characters")
	String identifyBy;
	@NotNull(message = "Identifiers is required")
	@Size(min = 0, max = 500, message = "name must be longer than 3 and less than 500 characters")
	String identifiers;
	@NotNull(message = "Username is required")
	@Size(min = 3, max = 50, message = "name must be longer than 3 and less than 40 characters")
	String username;
	@NotNull(message = "Password is required")
	@Size(min = 3, max = 50, message = "name must be longer than 3 and less than 40 characters")
	String password;
	@NotNull(message = "Enabled is required")
	boolean enabled;

	public ConfigDeviceType() {
		super();
	}
	
	

	public ConfigDeviceType(String group) {
		super();
		this.group = group;
	}



	public ConfigDeviceType(String group, String name, Boolean enabled) {
		super();
		this.group = group;
		this.name = name;
		this.enabled = enabled;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIdentifyBy() {
		return identifyBy;
	}

	public void setIdentifyBy(String identifyBy) {
		this.identifyBy = identifyBy;
	}

	public String getIdentifiers() {
		return identifiers;
	}

	public void setIdentifiers(String identifiers) {
		this.identifiers = identifiers;
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

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isPersisted() {
		return name != null;
	}

}
