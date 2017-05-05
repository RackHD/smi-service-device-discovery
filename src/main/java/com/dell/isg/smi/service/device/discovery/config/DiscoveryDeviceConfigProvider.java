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
import com.dell.isg.smi.commons.model.device.discovery.config.DiscoveryDevice;
import com.dell.isg.smi.commons.model.device.discovery.config.DiscoveryDeviceType;

@Component
public class DiscoveryDeviceConfigProvider {

    @Autowired
    DiscoveryDeviceConfig discoveryDeviceConfig;

    @Value("${command.arp.mac}")
    String ARP_COMMAND;

    EnumMap<DiscoveryDeviceGroupEnum, DiscoveryDevice> discoveryDeviceGroupMap = new EnumMap<DiscoveryDeviceGroupEnum, DiscoveryDevice>(DiscoveryDeviceGroupEnum.class);

    EnumMap<DiscoveryDeviceTypeEnum, DiscoveryDeviceType> discoveryDeviceTypeMap = new EnumMap<DiscoveryDeviceTypeEnum, DiscoveryDeviceType>(DiscoveryDeviceTypeEnum.class);


    @PostConstruct
    public void init() {
        for (DiscoveryDeviceGroupEnum enumGroupName : DiscoveryDeviceGroupEnum.values()) {
            discoveryDeviceGroupMap.put(enumGroupName, findDiscoveryDeviceByGroup(enumGroupName));
        }

        for (DiscoveryDeviceTypeEnum enumTypeName : DiscoveryDeviceTypeEnum.values()) {
            discoveryDeviceTypeMap.put(enumTypeName, findDiscoveryDeviceTypeByType(enumTypeName));
        }

    }


    @PreDestroy
    public void cleanup() {

    }


    public DiscoveryDevice getDiscoveryDevicesByGroup(DiscoveryDeviceGroupEnum deviceGroup) {
        DiscoveryDevice discoveryDevice = discoveryDeviceGroupMap.get(deviceGroup);
        if (discoveryDevice != null) {
            return discoveryDevice;
        }
        return findDiscoveryDeviceByGroup(deviceGroup);
    }


    private DiscoveryDevice findDiscoveryDeviceByGroup(DiscoveryDeviceGroupEnum deviceGroup) {
        List<DiscoveryDevice> discoveryDevices = (List<DiscoveryDevice>) CollectionUtils.select(discoveryDeviceConfig.getDiscoveryDevice(), predicateDiscoveryDevicebyGroup(deviceGroup.value()));
        if (CollectionUtils.isEmpty(discoveryDevices)) {
            return null;
        }
        return discoveryDevices.get(0);
    }


    public List<DiscoveryDeviceType> getDiscoveryDeviceTypeByGroup(DiscoveryDeviceGroupEnum discoveryDeviceGroupEnum) {
        return discoveryDeviceGroupMap.get(discoveryDeviceGroupEnum).getDiscoveryDeviceType();
    }


    public List<DiscoveryDeviceType> getAllDiscoveryDeviceTypes() {
        return discoveryDeviceTypeMap.values().stream().collect(Collectors.toList());
    }


    public List<String> getAllDeviceNameByGroup(DiscoveryDeviceGroupEnum discoveryDeviceGroupEnum) {
        List<String> deviceNameList = new ArrayList<String>();
        for (DiscoveryDeviceType discoveryDeviceType : getDiscoveryDeviceTypeByGroup(discoveryDeviceGroupEnum)) {
            deviceNameList.add(discoveryDeviceType.getDiscoveryDeviceName());
        }
        return deviceNameList;
    }


    public DiscoveryDeviceType getDiscoveryDeviceTypeByType(DiscoveryDeviceTypeEnum deviceType) {
        DiscoveryDeviceType discoveryDeviceType = discoveryDeviceTypeMap.get(deviceType);
        if (discoveryDeviceType != null) {
            return discoveryDeviceType;
        }
        return findDiscoveryDeviceTypeByType(deviceType);
    }


    public String getArpCommand() {
        return ARP_COMMAND;
    }


    private Predicate<DiscoveryDevice> predicateDiscoveryDevicebyGroup(String group) {
        return new Predicate<DiscoveryDevice>() {
            @Override
            public boolean evaluate(DiscoveryDevice discoveryDevice) {
                if (discoveryDevice == null) {
                    return false;
                }
                return discoveryDevice.getDiscoveryDeviceGroup().trim().equals(group);
            }
        };
    }


    private DiscoveryDeviceType findDiscoveryDeviceTypeByType(DiscoveryDeviceTypeEnum deviceType) {
        List<DiscoveryDeviceType> discoveryDeviceTypeAllList = new ArrayList<DiscoveryDeviceType>();
        for (DiscoveryDeviceGroupEnum enumGroupName : DiscoveryDeviceGroupEnum.values()) {
            discoveryDeviceTypeAllList.addAll(discoveryDeviceGroupMap.get(enumGroupName).getDiscoveryDeviceType());
        }

        List<DiscoveryDeviceType> discoveryDevicesListByType = (List<DiscoveryDeviceType>) CollectionUtils.select(discoveryDeviceTypeAllList, predicateDiscoveryDeviceTypebyType(deviceType.value()));

        if (CollectionUtils.isEmpty(discoveryDevicesListByType)) {
            return null;
        }

        return discoveryDevicesListByType.get(0);
    }


    private Predicate<DiscoveryDeviceType> predicateDiscoveryDeviceTypebyType(String type) {
        return new Predicate<DiscoveryDeviceType>() {
            @Override
            public boolean evaluate(DiscoveryDeviceType discoveryDeviceType) {
                if (discoveryDeviceType == null) {
                    return false;
                }
                return discoveryDeviceType.getDiscoveryDeviceName().trim().equals(type);
            }
        };
    }

}
