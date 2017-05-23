/**
 * Copyright © 2017 DELL Inc. or its subsidiaries.  All Rights Reserved.
 */
package com.dell.isg.smi.service.device.discovery.utilities;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dell.isg.smi.adapter.chassis.IChassisAdapter;
import com.dell.isg.smi.adapter.server.IServerAdapter;
import com.dell.isg.smi.adapter.server.model.WsmanCredentials;
import com.dell.isg.smi.common.protocol.command.cmc.entity.ChassisCMCViewEntity;
import com.dell.isg.smi.common.protocol.command.cmc.entity.RacadmCredentials;
import com.dell.isg.smi.commons.model.common.Credential;
import com.dell.isg.smi.commons.model.commons.chassis.inventory.ChassisSummary;
import com.dell.isg.smi.commons.model.device.discovery.DiscoveredDeviceInfo;
import com.dell.isg.smi.commons.model.device.discovery.DiscoveryDeviceStatus;
import com.dell.isg.smi.commons.model.device.discovery.DiscoveryDeviceTypeEnum;
import com.dell.isg.smi.commons.model.device.discovery.config.DeviceDefaultCredential;
import com.dell.isg.smi.commons.model.device.discovery.config.DiscoveryDeviceType;
import com.dell.isg.smi.commons.model.server.inventory.HwSystem;
import com.dell.isg.smi.service.device.discovery.config.DiscoveryDeviceConfigProvider;
import com.dell.isg.smi.service.device.discovery.manager.threads.RequestScopeDiscoveryCredential;
import com.dell.isg.smi.service.server.inventory.Transformer.TranformerUtil;
import com.dell.isg.smi.wsman.command.entity.DCIMSystemViewType;

@Component
public class ExtractDeviceSummaryUtil {

    private static IServerAdapter serverAdapterImpl;

    private static IChassisAdapter chassisAdapterImpl;

    private static DiscoveryDeviceConfigProvider discoveryDeviceConfigProvider;

    private static RequestScopeDiscoveryCredential requestScopeDiscoveryCredential;


    public static void extarctDeviceSummary(DiscoveredDeviceInfo discoverDeviceInfo) throws Exception {
        DiscoveryDeviceTypeEnum deviceTypeEnum = DiscoveryDeviceTypeEnum.valueOf(discoverDeviceInfo.getDeviceType());
        switch (deviceTypeEnum) {
        case IDRAC6:
        case IDRAC7:
        case IDRAC8:
        case IDRAC9:
            extractSummaryThouWsman(discoverDeviceInfo);
            break;
        case CMC:
        case CMC_FX2:
        case CSERVER:
        case VRTX:
            extractSummaryThouRacadm(discoverDeviceInfo);
            break;
        case FORCE10_S4810:
        case FORCE10_S5000:
        case FORCE10_S6000:
        case FORCE10_S4048:
        case FORCE10IOM:
        case FX2_IOM:
            break;
        case DELL_IOM_84:
        case BROCADE:
            break;
        case POWERCONNECT:
        case POWERCONNECT_N3000:
        case POWERCONNECT_N4000:
            break;
        case CISCONEXUS:
            break;
        case EQUALLOGIC_NODISCOVER:
        case EQUALLOGIC:
            break;
        case EM_COMPELLENT:
        case COMPELLENT:
            break;
        case VCENTER:
            break;
        default:
        }
    }


    private static void extractSummaryThouWsman(DiscoveredDeviceInfo discoverDeviceInfo) throws Exception {
        DiscoveryDeviceType discoveryDeviceType = discoveryDeviceConfigProvider.getDiscoveryDeviceTypeByType(DiscoveryDeviceTypeEnum.valueOf(discoverDeviceInfo.getDeviceType()));
        DeviceDefaultCredential deviceDefaultCredential = discoveryDeviceType.getDiscoveryRules().getDeviceDefaultCredential();
        String user = deviceDefaultCredential.getUser();
        String password = deviceDefaultCredential.getPassword();
        Credential credential = requestScopeDiscoveryCredential.getCredential(discoveryDeviceType.getDiscoveryDeviceName());
        if (credential != null && !StringUtils.isEmpty(credential.getUserName())) {
            user = credential.getUserName();
            password = credential.getPassword();
        }
        WsmanCredentials wsmanCredential = new WsmanCredentials(discoverDeviceInfo.getIpAddress(), user, password);
        DCIMSystemViewType system = serverAdapterImpl.collectSystemInfo(wsmanCredential);
        if (system == null) {
            throw new Exception();
        }
        HwSystem summary = new HwSystem();
        summary.setId(discoverDeviceInfo.getIpAddress());
        TranformerUtil.transformServerSummary(system, summary);
        discoverDeviceInfo.setSummary(summary);
        discoverDeviceInfo.setStatus(DiscoveryDeviceStatus.SUCCESS.getValue());
    }


    private static void extractSummaryThouRacadm(DiscoveredDeviceInfo discoverDeviceInfo) throws Exception {
        DiscoveryDeviceType discoveryDeviceType = discoveryDeviceConfigProvider.getDiscoveryDeviceTypeByType(DiscoveryDeviceTypeEnum.valueOf(discoverDeviceInfo.getDeviceType()));
        DeviceDefaultCredential deviceDefaultCredential = discoveryDeviceType.getDiscoveryRules().getDeviceDefaultCredential();
        String user = deviceDefaultCredential.getUser();
        String password = deviceDefaultCredential.getPassword();
        Credential credential = requestScopeDiscoveryCredential.getCredential(discoveryDeviceType.getDiscoveryDeviceName());
        if (credential != null && !StringUtils.isEmpty(credential.getUserName())) {
            user = credential.getUserName();
            password = credential.getPassword();
        }
        RacadmCredentials racadmCredentials = new RacadmCredentials(discoverDeviceInfo.getIpAddress(), user, password, false);
        ChassisCMCViewEntity chassis = chassisAdapterImpl.collectChassisSummary(racadmCredentials);
        if (chassis == null) {
            throw new Exception();
        }
        ChassisSummary summary = TranformerUtil.transformChassisSummary(chassis);
        summary.setId(discoverDeviceInfo.getIpAddress());
        discoverDeviceInfo.setSummary(summary);
        discoverDeviceInfo.setStatus(DiscoveryDeviceStatus.SUCCESS.getValue());
    }


    @Autowired(required = true)
    public void setIServerAdapter(IServerAdapter serverAdapterImpl) {
        ExtractDeviceSummaryUtil.serverAdapterImpl = serverAdapterImpl;
    }


    public static IServerAdapter getIServerAdapter() {
        return serverAdapterImpl;
    }


    @Autowired(required = true)
    public void setIChassisAdapter(IChassisAdapter chassisAdapterImpl) {
        ExtractDeviceSummaryUtil.chassisAdapterImpl = chassisAdapterImpl;
    }


    public static IChassisAdapter getIChassisAdapter() {
        return chassisAdapterImpl;
    }


    @Autowired(required = true)
    public void setDiscoveryDeviceConfigProvider(DiscoveryDeviceConfigProvider discoveryDeviceConfigProvider) {
        ExtractDeviceSummaryUtil.discoveryDeviceConfigProvider = discoveryDeviceConfigProvider;
    }


    public static DiscoveryDeviceConfigProvider getDiscoveryDeviceConfigProvider() {
        return discoveryDeviceConfigProvider;
    }


    public static RequestScopeDiscoveryCredential getRequestScopeDiscoveryCredential() {
        return requestScopeDiscoveryCredential;
    }


    @Autowired(required = true)
    public void setRequestScopeDiscoveryCredential(RequestScopeDiscoveryCredential requestScopeDiscoveryCredential) {
        ExtractDeviceSummaryUtil.requestScopeDiscoveryCredential = requestScopeDiscoveryCredential;
    }

}
