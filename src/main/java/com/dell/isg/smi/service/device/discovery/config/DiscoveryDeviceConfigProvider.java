/**
 * Copyright © 2017 DELL Inc. or its subsidiaries.  All Rights Reserved.
 */
package com.dell.isg.smi.service.device.discovery.config;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.dell.isg.smi.commons.model.device.discovery.DiscoveryDeviceGroupEnum;
import com.dell.isg.smi.commons.model.device.discovery.DiscoveryDeviceTypeEnum;
import com.dell.isg.smi.commons.model.device.discovery.config.DeviceGroup;
import com.dell.isg.smi.commons.model.device.discovery.config.DeviceType;
import com.dell.isg.smi.commons.model.device.discovery.config.DiscoveryRule;

@Component
public class DiscoveryDeviceConfigProvider {

	@Autowired
	DiscoveryDeviceConfig discoveryDeviceConfig;

	@Value("${command.arp.mac}")
	String ARP_COMMAND;

	EnumMap<DiscoveryDeviceGroupEnum, DeviceGroup> discoveryDeviceGroupMap = new EnumMap<DiscoveryDeviceGroupEnum, DeviceGroup>(
			DiscoveryDeviceGroupEnum.class);

	EnumMap<DiscoveryDeviceTypeEnum, DeviceType> discoveryDeviceTypeMap = new EnumMap<DiscoveryDeviceTypeEnum, DeviceType>(
			DiscoveryDeviceTypeEnum.class);

	@PostConstruct
	public void init() {
		for (DiscoveryDeviceGroupEnum enumGroupName : DiscoveryDeviceGroupEnum.values()) {
			discoveryDeviceGroupMap.put(enumGroupName, findDeviceGroup(enumGroupName));
		}

		for (DiscoveryDeviceTypeEnum enumTypeName : DiscoveryDeviceTypeEnum.values()) {
			discoveryDeviceTypeMap.put(enumTypeName, findDeviceTypeByTypeEnum(enumTypeName));
		}

	}

	@PreDestroy
	public void cleanup() {

	}

	public DeviceGroup getDeviceGroup(DiscoveryDeviceGroupEnum deviceGroupEnum) {
		DeviceGroup deviceGroup = discoveryDeviceGroupMap.get(deviceGroupEnum);
		if (deviceGroup != null) {
			return deviceGroup;
		}
		return findDeviceGroup(deviceGroupEnum);
	}

	private DeviceGroup findDeviceGroup(DiscoveryDeviceGroupEnum deviceGroupEnum) {
		List<DeviceGroup> deviceGroupList = (List<DeviceGroup>) CollectionUtils
				.select(discoveryDeviceConfig.getDeviceGroup(), predicateDeviceGroup(deviceGroupEnum.value()));
		if (CollectionUtils.isEmpty(deviceGroupList)) {
			return null;
		}
		return deviceGroupList.get(0);
	}

	public List<DeviceType> getDeviceTypeByGroup(DiscoveryDeviceGroupEnum deviceGroupEnum) {
		List<DeviceType> deviceTypeList = new ArrayList<DeviceType>();
		for (DiscoveryRule discoveryRule : discoveryDeviceGroupMap.get(deviceGroupEnum).getDiscoveryRule()) {
			deviceTypeList.addAll(discoveryRule.getDeviceType());
		}
		return deviceTypeList;
	}
	
	public List<DiscoveryRule> getAllDiscoveryRuleByGroup(DiscoveryDeviceGroupEnum deviceGroupEnum) {	
		return discoveryDeviceGroupMap.get(deviceGroupEnum).getDiscoveryRule();
	}

	public List<DeviceType> getAllDeviceType() {
		return discoveryDeviceTypeMap.values().stream().collect(Collectors.toList());
	}

	public List<String> getAllDeviceTypeNameByGroup(DiscoveryDeviceGroupEnum deviceGroupEnum) {
		List<String> deviceNameList = new ArrayList<String>();
		for (DeviceType deviceType : getDeviceTypeByGroup(deviceGroupEnum)) {
			deviceNameList.add(deviceType.getName());
		}
		return deviceNameList;
	}

	public DeviceType getDeviceType(DiscoveryDeviceTypeEnum deviceTypeEnum) {
		DeviceType deviceType = discoveryDeviceTypeMap.get(deviceTypeEnum);
		if (deviceType != null) {
			return deviceType;
		}
		return findDeviceTypeByTypeEnum(deviceTypeEnum);
	}

	public String getArpCommand() {
		return ARP_COMMAND;
	}

	private Predicate<DeviceGroup> predicateDeviceGroup(String group) {
		return new Predicate<DeviceGroup>() {
			@Override
			public boolean evaluate(DeviceGroup deviceGroup) {
				if (deviceGroup == null) {
					return false;
				}
				return deviceGroup.getGroupName().trim().equals(group);
			}
		};
	}

	private DeviceType findDeviceTypeByTypeEnum(DiscoveryDeviceTypeEnum deviceTypeEnum) {
		List<DeviceType> deviceTypeList = new ArrayList<DeviceType>();
		for (DiscoveryDeviceGroupEnum deviceGroupEnum : DiscoveryDeviceGroupEnum.values()) {
			for (DiscoveryRule discoveryRule : discoveryDeviceGroupMap.get(deviceGroupEnum).getDiscoveryRule()) {
				deviceTypeList.addAll(discoveryRule.getDeviceType());
			}
		}

		List<DeviceType> deviceTypeListByEnum = (List<DeviceType>) CollectionUtils
				.select(deviceTypeList, predicateDeviceType(deviceTypeEnum.value()));

		if (CollectionUtils.isEmpty(deviceTypeListByEnum)) {
			return null;
		}

		return deviceTypeListByEnum.get(0);
	}

	private Predicate<DeviceType> predicateDeviceType(String type) {
		return new Predicate<DeviceType>() {
			@Override
			public boolean evaluate(DeviceType deviceType) {
				if (deviceType == null) {
					return false;
				}
				return deviceType.getName().trim().equals(type);
			}
		};
	}

}
