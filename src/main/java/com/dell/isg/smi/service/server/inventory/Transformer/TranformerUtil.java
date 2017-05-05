/**
 * Copyright © 2017 DELL Inc. or its subsidiaries.  All Rights Reserved.
 */
package com.dell.isg.smi.service.server.inventory.Transformer;

import com.dell.isg.smi.common.protocol.command.cmc.entity.ChassisCMCViewEntity;
import com.dell.isg.smi.commons.elm.utilities.DateTimeUtils;
import com.dell.isg.smi.commons.model.commons.chassis.inventory.ChassisSummary;
import com.dell.isg.smi.commons.model.server.inventory.HwSystem;
import com.dell.isg.smi.service.device.discovery.utilities.PatternUtils;
import com.dell.isg.smi.wsman.command.entity.DCIMSystemViewType;

public class TranformerUtil {

    public static HwSystem transformServerSummary(DCIMSystemViewType system, HwSystem hwSystem) throws Exception {
        if (system == null) {
            return null;
        }
        hwSystem.setAssetTag(system.getAssetTag() != null ? system.getAssetTag().getValue() : null);
        hwSystem.setBatteryRollupStatus(system.getBatteryRollupStatus() != null ? String.valueOf(system.getBatteryRollupStatus().getValue()) : null);
        if (system.getBiosReleaseDate() != null) {
            hwSystem.setBiosReleaseDate(DateTimeUtils.getIsoDateString(system.getBiosReleaseDate().getValue(), TransformerAssemblerConstants.DATE_PATTERNS));
        }
        hwSystem.setBiosVersionString(system.getBiosVersionString() != null ? system.getBiosVersionString().getValue() : null);
        hwSystem.setBoardPartNumber(system.getBoardPartNumber() != null ? system.getBoardPartNumber().getValue() : null);
        hwSystem.setBoardSerialNumber(system.getBoardSerialNumber() != null ? system.getBoardSerialNumber().getValue() : null);
        hwSystem.setChassisName(system.getChassisName() != null ? system.getChassisName().getValue() : null);
        hwSystem.setChassisServiceTag(system.getChassisServiceTag() != null ? system.getChassisServiceTag().getValue() : null);
        hwSystem.setChassisSystemHeight(system.getChassisSystemHeight() != null ? String.valueOf(system.getChassisSystemHeight().getValue()) : null);
        hwSystem.setCmcip(system.getCmcip() != null ? system.getCmcip().getValue() : null);
        hwSystem.setCpldVersion(system.getCpldVersion() != null ? system.getCpldVersion().getValue() : null);
        hwSystem.setDeviceDescription(system.getDescription() != null ? system.getDescription().getValue() : null);
        hwSystem.setFqdd(system.getFqdd() != null ? system.getFqdd().getValue() : null);
        hwSystem.setHostName(system.getHostName() != null ? system.getHostName().getValue() : null);
        hwSystem.setInstanceID(system.getInstanceID() != null ? system.getInstanceID().getValue() : null);
        hwSystem.setLicensingRollupStatus(system.getLicensingRollupStatus() != null ? String.valueOf(system.getLicensingRollupStatus().getValue()) : null);
        hwSystem.setLifecycleControllerVersion(system.getLifecycleControllerVersion() != null ? system.getLifecycleControllerVersion().getValue() : null);
        hwSystem.setManufacturer(system.getManufacturer() != null ? system.getManufacturer().getValue() : null);
        hwSystem.setMaxCpuSockets(system.getMaxCPUSockets() != null ? String.valueOf(system.getMaxCPUSockets().getValue()) : null);
        hwSystem.setMaxDimmSlots(system.getMaxDIMMSlots() != null ? String.valueOf(system.getMaxDIMMSlots().getValue()) : null);
        hwSystem.setMaxPcieSlots(system.getMaxPCIeSlots() != null ? String.valueOf(system.getMaxPCIeSlots().getValue()) : null);
        hwSystem.setMemoryOperationMode(system.getMemoryOperationMode() != null ? system.getMemoryOperationMode().getValue() : null);
        hwSystem.setModel(system.getModel() != null ? system.getModel().getValue() : null);
        hwSystem.setPlatformGuid(system.getPlatformGUID() != null ? system.getPlatformGUID().getValue() : null);
        hwSystem.setPopulatedCpuSockets(system.getPopulatedCPUSockets() != null ? String.valueOf(system.getPopulatedCPUSockets().getValue()) : null);
        hwSystem.setPopulatedDimmSlots(system.getPopulatedDIMMSlots() != null ? String.valueOf(system.getPopulatedDIMMSlots().getValue()) : null);
        hwSystem.setPopulatedPcieSlots(system.getPopulatedPCIeSlots() != null ? String.valueOf(system.getPopulatedPCIeSlots().getValue()) : null);
        hwSystem.setPowerCap(system.getPowerCap() != null ? String.valueOf(system.getPowerCap().getValue()) : null);
        hwSystem.setPowerCapEnabledState(system.getPowerCapEnabledState() != null ? String.valueOf(system.getPowerCapEnabledState().getValue()) : null);
        hwSystem.setPowerState(system.getPowerState() != null ? String.valueOf(system.getPowerState().getValue()) : null);
        hwSystem.setPsRollupStatus(system.getPsRollupStatus() != null ? String.valueOf(system.getPsRollupStatus().getValue()) : null);
        hwSystem.setRollupStatus(system.getRollupStatus() != null ? String.valueOf(system.getRollupStatus().getValue()) : null);
        hwSystem.setServiceTag(system.getServiceTag() != null ? system.getServiceTag().getValue() : null);
        hwSystem.setSysMemLocation(system.getSysMemLocation() != null ? String.valueOf(system.getSysMemLocation().getValue()) : null);
        hwSystem.setSysMemErrorInfo(system.getSysMemErrorInfo() != null ? String.valueOf(system.getSysMemErrorInfo().getValue()) : null);
        hwSystem.setSysMemErrorMethodology(system.getSysMemErrorMethodology() != null ? String.valueOf(system.getSysMemErrorMethodology().getValue()) : null);
        hwSystem.setSysMemPrimaryStatus(system.getSysMemPrimaryStatus() != null ? String.valueOf(system.getSysMemPrimaryStatus().getValue()) : null);
        // convert Installed Capacity to GB from MB
        hwSystem.setSysMemTotalSize(system.getSysMemTotalSize() != null ? PatternUtils.megaBytesToGigaBytes(system.getSysMemTotalSize().getValue()) : 0);
        hwSystem.setSystemGeneration(system.getSystemGeneration() != null ? String.valueOf(system.getSystemGeneration().getValue()) : null);
        hwSystem.setSystemID(system.getSystemID() != null ? String.valueOf(system.getSystemID().getValue()) : null);
        hwSystem.setSysMemMaxCapacitySize(system.getSysMemMaxCapacitySize() != null ? String.valueOf(system.getSysMemMaxCapacitySize().getValue()) : null);
        hwSystem.setUuid(system.getUuid() != null ? system.getUuid().getValue() : null);
        hwSystem.setChassisModel(system.getChassisModel() != null ? system.getChassisModel().getValue() : null);
        hwSystem.setDeviceDescription(system.getDeviceDescription() != null ? system.getDeviceDescription().getValue() : null);
        return hwSystem;
    }


    public static ChassisSummary transformChassisSummary(ChassisCMCViewEntity chassisCMCViewEntity) {
        ChassisSummary chassis = new ChassisSummary();
        chassis.setServiceTag(chassisCMCViewEntity.getServiceTag());
        chassis.setLocation(chassisCMCViewEntity.getChassisLocation());
        chassis.setModel(chassisCMCViewEntity.getSystemModel());
        chassis.setName(chassisCMCViewEntity.getChassisName());
        chassis.setDnsName(chassisCMCViewEntity.getDnsCMCName());
        return chassis;

    }

}
