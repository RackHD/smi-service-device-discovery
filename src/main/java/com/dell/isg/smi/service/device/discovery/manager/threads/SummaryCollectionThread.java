/**
 * Copyright © 2017 DELL Inc. or its subsidiaries.  All Rights Reserved.
 */
package com.dell.isg.smi.service.device.discovery.manager.threads;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dell.isg.smi.commons.model.device.discovery.DiscoveredDeviceInfo;
import com.dell.isg.smi.commons.model.device.discovery.DiscoveryDeviceStatus;
import com.dell.isg.smi.service.device.discovery.utilities.ExtractDeviceSummaryUtil;

public class SummaryCollectionThread implements Runnable {
    private DiscoveredDeviceInfo discoverDeviceInfo;

    private static final Logger logger = LoggerFactory.getLogger(SummaryCollectionThread.class.getName());


    public SummaryCollectionThread(DiscoveredDeviceInfo discoverDeviceInfo) {
        this.discoverDeviceInfo = discoverDeviceInfo;
    }


    @Override
    public void run() {
        try {
            processCommand();
        } catch (Exception e) {
            logger.error(" Discovery Failed Reason for :" + e.getMessage());
        }
    }


    private void processCommand() throws Exception {
        discoverDeviceInfo.setStatus(DiscoveryDeviceStatus.SUMMARY_INPROGRESS.getValue());
        logger.trace("Started summary extraction for IP : " + discoverDeviceInfo.getIpAddress() + " :for device type -->" + discoverDeviceInfo.getDeviceType());
        try {
            ExtractDeviceSummaryUtil.extarctDeviceSummary(discoverDeviceInfo);
            logger.trace("Completed summary extraction for IP : " + discoverDeviceInfo.getIpAddress() + " :for device type -->" + discoverDeviceInfo.getDeviceType());
        } catch (Exception e) {
            logger.trace("Failed summary extraction for IP : " + discoverDeviceInfo.getIpAddress() + " :for device type -->" + discoverDeviceInfo.getDeviceType(), e);
        } finally {
            if (discoverDeviceInfo.getSummary() == null) {
                discoverDeviceInfo.setStatus(DiscoveryDeviceStatus.DEVICE_DISCOVERED_SUMMARY_FAILED.getValue());
            }
        }
    }

}
