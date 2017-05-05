/**
 * Copyright © 2017 DELL Inc. or its subsidiaries.  All Rights Reserved.
 */
package com.dell.isg.smi.service.device.discovery.manager.threads;

import java.util.List;

import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dell.isg.smi.commons.model.device.discovery.DiscoveredDeviceInfo;
import com.dell.isg.smi.commons.model.device.discovery.DiscoveryDeviceGroupEnum;
import com.dell.isg.smi.commons.model.device.discovery.DiscoveryDeviceProtocolEnum;
import com.dell.isg.smi.commons.model.device.discovery.DiscoveryDeviceStatus;
import com.dell.isg.smi.commons.model.device.discovery.DiscoveryDeviceTypeEnum;
import com.dell.isg.smi.commons.model.device.discovery.config.DiscoveryDeviceType;
import com.dell.isg.smi.service.device.discovery.config.DiscoveryDeviceConfigProvider;
import com.dell.isg.smi.service.device.discovery.utilities.DiscoverDeviceTypeUtil;
import com.dell.isg.smi.service.device.discovery.validation.IPRangeValidatorUtil;

public class DeviceIdentificationThread implements Runnable {
    private DiscoveredDeviceInfo discoverDeviceInfo;
    private DiscoveryDeviceConfigProvider discoveryDeviceConfigProvider;
    private DiscoveryDeviceGroupEnum discoveryDeviceGroupEnum;

    private static final Logger logger = LoggerFactory.getLogger(DeviceIdentificationThread.class.getName());


    public DeviceIdentificationThread(DiscoveredDeviceInfo response, DiscoveryDeviceConfigProvider configProvider, DiscoveryDeviceGroupEnum discoveryDeviceGroupEnum) {
        this.discoverDeviceInfo = response;
        this.discoveryDeviceConfigProvider = configProvider;
        this.discoveryDeviceGroupEnum = discoveryDeviceGroupEnum;
    }


    @Override
    public void run() {
        try {
            processCommand();
        } catch (Exception e) {
            logger.error(" Device idntification failed :" + e.getMessage());
        }
    }


    private void processCommand() throws Exception {
        String ipAddress = discoverDeviceInfo.getIpAddress();
        logger.trace("Started device identification for IP : " + ipAddress);
        if (IPRangeValidatorUtil.ping(ipAddress)) {
            discoverDeviceInfo.setMacAddress(IPRangeValidatorUtil.getMacAddressForIp(ipAddress, discoveryDeviceConfigProvider.getArpCommand()));
            identifyDeviceByIP(ipAddress);
        } else {
            discoverDeviceInfo.setDeviceType(DiscoveryDeviceTypeEnum.UNKNOWN.value());
            discoverDeviceInfo.setStatus(DiscoveryDeviceStatus.NO_DEVICE.name());
        }
    }


    private void identifyDeviceByIP(String ipAddress) {

        if (discoveryDeviceGroupEnum != null && EnumUtils.isValidEnum(DiscoveryDeviceGroupEnum.class, discoveryDeviceGroupEnum.value())) {
            amI(ipAddress, discoveryDeviceGroupEnum);
        } else if (amI(ipAddress, DiscoveryDeviceGroupEnum.SERVER)) {
            logger.trace(ipAddress + " Identified has Server Type: " + discoverDeviceInfo.getDeviceType());
        } else if (amI(ipAddress, DiscoveryDeviceGroupEnum.CHASSIS)) {
            logger.trace(ipAddress + " Identified has Chassis Type: " + discoverDeviceInfo.getDeviceType());
        } else if (amI(ipAddress, DiscoveryDeviceGroupEnum.SWITCH)) {
            logger.trace(ipAddress + " Identified has Switch Type: " + discoverDeviceInfo.getDeviceType());
        } else if (amI(ipAddress, DiscoveryDeviceGroupEnum.STORAGE)) {
            logger.trace(ipAddress + " Identified has Storage Type: " + discoverDeviceInfo.getDeviceType());
        } else if (amI(ipAddress, DiscoveryDeviceGroupEnum.IOM)) {
            logger.trace(ipAddress + " Identified has IOM Type: " + discoverDeviceInfo.getDeviceType());
        } else if (amI(ipAddress, DiscoveryDeviceGroupEnum.VM)) {
            logger.trace(ipAddress + " Identified has VM Type: " + discoverDeviceInfo.getDeviceType());
        } else {
            discoverDeviceInfo.setDeviceType(DiscoveryDeviceTypeEnum.UNKNOWN.value());
            discoverDeviceInfo.setStatus(DiscoveryDeviceStatus.UNKNOWN.name());
        }
    }


    private boolean amI(String ipAddress, DiscoveryDeviceGroupEnum group) {
        boolean isDeviceIdentified = false;
        List<DiscoveryDeviceType> discoveryDeviceTypeList = discoveryDeviceConfigProvider.getDiscoveryDeviceTypeByGroup(group);
        for (DiscoveryDeviceType discoveryDeviceType : discoveryDeviceTypeList) {
            String protocol = discoveryDeviceType.getDiscoveryRules().getProtocol();
            if (identify(ipAddress, DiscoveryDeviceProtocolEnum.fromValue(protocol), discoveryDeviceType)) {
                discoverDeviceInfo.setDeviceType(discoveryDeviceType.getDiscoveryDeviceName());
                discoverDeviceInfo.setStatus(DiscoveryDeviceStatus.DEVICE_IDENTFIED.name());
                isDeviceIdentified = true;
            }
        }
        return isDeviceIdentified;

    }


    private boolean identify(String ipAddress, DiscoveryDeviceProtocolEnum protocol, DiscoveryDeviceType discoveryDeviceType) {
        boolean isDeviceIdentified = false;
        switch (protocol) {
        case HTTP:
        case HTTPS:
            isDeviceIdentified = DiscoverDeviceTypeUtil.processHttps(ipAddress, discoveryDeviceType);
            break;
        case TCP:
            isDeviceIdentified = DiscoverDeviceTypeUtil.processTcp(ipAddress, discoveryDeviceType);
            break;
        case SSH:
            isDeviceIdentified = DiscoverDeviceTypeUtil.processSsh(ipAddress, discoveryDeviceType);
            break;
        case TFTP:
            isDeviceIdentified = DiscoverDeviceTypeUtil.processTftp(ipAddress, discoveryDeviceType);
            break;
        default:
            isDeviceIdentified = false;
        }
        return isDeviceIdentified;
    }
}
