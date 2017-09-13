/**
 * Copyright © 2017 DELL Inc. or its subsidiaries.  All Rights Reserved.
 */
package com.dell.isg.smi.service.device.discovery.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import com.dell.isg.smi.commons.utilities.CustomRecursiveToStringStyle;
import com.dell.isg.smi.commons.elm.exception.InvalidArgumentsException;
import com.dell.isg.smi.commons.model.common.Credential;
import com.dell.isg.smi.commons.model.common.DevicesIpsRequest;
import com.dell.isg.smi.commons.model.device.discovery.DiscoverDeviceRequest;
import com.dell.isg.smi.commons.model.device.discovery.DiscoverIPRangeDeviceRequests;
import com.dell.isg.smi.commons.model.device.discovery.DiscoverdDeviceResponse;
import com.dell.isg.smi.commons.model.device.discovery.DiscoveredDeviceInfo;
import com.dell.isg.smi.commons.model.device.discovery.DiscoveredDeviceTypes;
import com.dell.isg.smi.commons.model.device.discovery.DiscoveryDeviceGroupEnum;
import com.dell.isg.smi.commons.model.device.discovery.DiscoveryDeviceStatus;
import com.dell.isg.smi.commons.model.device.discovery.DiscoveryDeviceTypeEnum;
import com.dell.isg.smi.service.device.discovery.config.DiscoveryDeviceConfigProvider;
import com.dell.isg.smi.service.device.discovery.manager.threads.DeviceIdentificationThread;
import com.dell.isg.smi.service.device.discovery.manager.threads.RequestScopeDiscoveryCredential;
import com.dell.isg.smi.service.device.discovery.manager.threads.SummaryCollectionThread;
import com.dell.isg.smi.service.device.discovery.validation.IPRangeValidatorUtil;
import com.dell.isg.smi.service.device.discovery.validation.Inet4ConverterValidator;

@Component
public class DiscoveryManagerImpl implements IDiscoveryManager {

    @Autowired
    DiscoveryDeviceConfigProvider discoveryDeviceConfigProvider;

    @Autowired
    RequestScopeDiscoveryCredential requestScopeDiscoveryCredential;

    private static final Logger logger = LoggerFactory.getLogger(DiscoveryManagerImpl.class.getName());
    private int IP_PING_THREAD_POOL = 22000;
    private int DISCOVER_THREAD_POOL = 2000;

    @Override
    public List<DiscoverdDeviceResponse> discover(DiscoverIPRangeDeviceRequests discoverIPRangeDeviceRequests) throws Exception {
        Credential globalCredential = discoverIPRangeDeviceRequests.getCredential();
        List<DiscoveredDeviceInfo> discoverDeviceInfosSummaryList = new ArrayList<DiscoveredDeviceInfo>();
        Set<DiscoverDeviceRequest> discoverDeviceRequests = discoverIPRangeDeviceRequests.getDiscoverIpRangeDeviceRequests();
        Set<String> discoverGroupSummaryFilter = new HashSet<String>();
        if (!CollectionUtils.isEmpty(discoverDeviceRequests)) {
            for (DiscoverDeviceRequest discoverDeviceRequest : discoverDeviceRequests) {
                List<DiscoveredDeviceInfo> discoverDeviceInfos = getValidDevicesForDiscovery(discoverDeviceRequest);
                Credential rangeCredential = discoverDeviceRequest.getCredential();
                String[] discoverGroupNames = discoverDeviceRequest.getDeviceType();
                overrideCredentials(globalCredential, rangeCredential, discoverGroupNames);
                if (ArrayUtils.isEmpty(discoverGroupNames)) {
                    discoverGroupNames = Stream.of(DiscoveryDeviceGroupEnum.values()).map(DiscoveryDeviceGroupEnum::name).toArray(String[]::new);
                }
                discoverGroupSummaryFilter.addAll(Arrays.asList( discoverGroupNames ));
                List<DiscoveredDeviceInfo> discoveredDeviceInfos = new ArrayList<DiscoveredDeviceInfo>();
                for (String discoverGroupName : discoverGroupNames) {
                    if (EnumUtils.isValidEnum(DiscoveryDeviceGroupEnum.class, discoverGroupName) && !StringUtils.equals(discoverGroupName, DiscoveryDeviceTypeEnum.UNKNOWN.value())) {
                        DiscoveryDeviceGroupEnum discoverGroup = DiscoveryDeviceGroupEnum.valueOf(discoverGroupName);
                        List<DiscoveredDeviceInfo> filteredList = (List<DiscoveredDeviceInfo>) CollectionUtils.select(discoverDeviceInfos, predicateReachableUndiscoveredDevice());
                        if (identifyDeviceType(filteredList, discoverGroup)) {
                            for (String deviceName : discoveryDeviceConfigProvider.getAllDeviceTypeNameByGroup(discoverGroup)) {
                                discoveredDeviceInfos.addAll(CollectionUtils.select(discoverDeviceInfos, predicateDeviceInfo(deviceName)));
                            }
                        }
                    }
                }
                discoverDeviceInfosSummaryList.addAll(runSummaryCollection(discoveredDeviceInfos));
            }
        }
        return getDiscoveredDeviceSummary(discoverDeviceInfosSummaryList, discoverGroupSummaryFilter);
    }

    @Override
    public List<DiscoverdDeviceResponse> discover(DevicesIpsRequest deviceIps) throws Exception {
        List<DiscoveredDeviceInfo> discoverDeviceInfos = new ArrayList<DiscoveredDeviceInfo>();
        Credential globalCredential = deviceIps.getCredential();
        String[] discoverGroupNames = deviceIps.getDeviceType();
        Set<String> discoverGroupSummaryFilter = new HashSet<String>();
        if (globalCredential != null && !StringUtils.isEmpty(globalCredential.getUserName())) {
            overrideRangeCredentials(globalCredential, deviceIps.getDeviceType());
        }
        Set<String> ips = Arrays.stream(deviceIps.getIps()).collect(Collectors.toSet());
        for (String validIp : ips) {
        	if (!Inet4ConverterValidator.isValidIpAddress(validIp)) {
        		String msg = "IPs";
                throw new InvalidArgumentsException(msg);
        	}
            DiscoveredDeviceInfo deviceInfo = new DiscoveredDeviceInfo();
            deviceInfo.setIpAddress(validIp);
            discoverDeviceInfos.add(deviceInfo);
        }
        if (ArrayUtils.isEmpty(discoverGroupNames)) {
            discoverGroupNames = Stream.of(DiscoveryDeviceGroupEnum.values()).map(DiscoveryDeviceGroupEnum::name).toArray(String[]::new);
        }
        discoverGroupSummaryFilter.addAll(Arrays.asList( discoverGroupNames ));

        List<DiscoveredDeviceInfo> discoveredDeviceInfos = new ArrayList<DiscoveredDeviceInfo>();
        for (String discoverGroupName : discoverGroupNames) {
            if (EnumUtils.isValidEnum(DiscoveryDeviceGroupEnum.class, discoverGroupName) && !StringUtils.equals(discoverGroupName, DiscoveryDeviceTypeEnum.UNKNOWN.value())) {
                DiscoveryDeviceGroupEnum discoverGroup = DiscoveryDeviceGroupEnum.valueOf(discoverGroupName);
                List<DiscoveredDeviceInfo> filteredList = (List<DiscoveredDeviceInfo>) CollectionUtils.select(discoverDeviceInfos, predicateReachableUndiscoveredDevice());
                if (identifyDeviceType(filteredList, discoverGroup)) {
                    for (String deviceName : discoveryDeviceConfigProvider.getAllDeviceTypeNameByGroup(discoverGroup)) {
                        discoveredDeviceInfos.addAll(CollectionUtils.select(discoverDeviceInfos, predicateDeviceInfo(deviceName)));
                    }
                }
            }
        }
        runSummaryCollection(discoveredDeviceInfos);
        return getDiscoveredDeviceSummary(discoveredDeviceInfos, discoverGroupSummaryFilter);
    }


    private boolean identifyDeviceType(List<DiscoveredDeviceInfo> discoverDeviceInfos, DiscoveryDeviceGroupEnum discoveryDeviceGroupEnum) {
        logger.trace("Started device identification threads");
        StopWatch watch = new StopWatch();
        watch.start();
        ExecutorService executor = Executors.newFixedThreadPool(IP_PING_THREAD_POOL);
        for (DiscoveredDeviceInfo discoverDeviceResponse : discoverDeviceInfos) {
            if (StringUtils.equalsIgnoreCase(discoverDeviceResponse.getDeviceType(), DiscoveryDeviceTypeEnum.UNKNOWN.name())) {
                Runnable discoveryIdentificationTask = new DeviceIdentificationThread(discoverDeviceResponse, discoveryDeviceConfigProvider, discoveryDeviceGroupEnum);
                executor.execute(discoveryIdentificationTask);
            }
        }
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.MINUTES);
            executor.shutdownNow();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            return false;
        }
        watch.stop();
        logger.trace("Finished all device identification threads");
        logger.trace("Time taken for device identification in seconds: " + watch.getTotalTimeSeconds());
        return true;
    }


    private List<DiscoveredDeviceInfo> getValidDevicesForDiscovery(DiscoverDeviceRequest discoverDeviceRequest) throws Exception {
        Set<String> validIpList = new HashSet<String>(IPRangeValidatorUtil.expandIpAddresses(discoverDeviceRequest));
        List<DiscoveredDeviceInfo> discoveryDeviceInfoList = new ArrayList<DiscoveredDeviceInfo>();
        for (String validIp : validIpList) {
            DiscoveredDeviceInfo discoveryDeviceResponse = new DiscoveredDeviceInfo();
            discoveryDeviceResponse.setIpAddress(validIp);
            discoveryDeviceInfoList.add(discoveryDeviceResponse);
        }
        return discoveryDeviceInfoList;

    }


    private List<DiscoveredDeviceInfo> runSummaryCollection(List<DiscoveredDeviceInfo> discoverDeviceInfos) {
        StopWatch watch = new StopWatch();
        watch.start();
        String[] devices = Stream.of(DiscoveryDeviceTypeEnum.values()).map(DiscoveryDeviceTypeEnum::name).toArray(String[]::new);
        logger.trace(" Expanded range ip count = " + discoverDeviceInfos.size());
        Collection<DiscoveredDeviceInfo> filteredList = CollectionUtils.select(discoverDeviceInfos, predicateDeviceInfos(Arrays.asList(devices)));
        int count = filteredList.size();
        logger.trace(" Device count for Summary Extraction = " + count);
        if (count > 0) {
            ExecutorService executor = Executors.newFixedThreadPool(DISCOVER_THREAD_POOL);
            for (DiscoveredDeviceInfo discoverDeviceInfo : filteredList) {
                Runnable discoverTask = new SummaryCollectionThread(discoverDeviceInfo);
                executor.execute(discoverTask);
            }
            executor.shutdown();
            try {
                executor.awaitTermination(60, TimeUnit.SECONDS);
                executor.shutdownNow();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }

        watch.stop();
        logger.trace("Finished device discovery threads.");
        logger.trace("Time taken for discovery in seconds: " + watch.getTotalTimeSeconds());
        return discoverDeviceInfos;
    }


    private List<DiscoverdDeviceResponse> getDiscoveredDeviceSummary(List<DiscoveredDeviceInfo> discoveredDeviceInfos, Set<String> discoverGroupSummaryFilter) {
        List<DiscoverdDeviceResponse> discoverdDeviceResponseList = new ArrayList<DiscoverdDeviceResponse>();
        for (String groupName : discoverGroupSummaryFilter) {
        	DiscoveryDeviceGroupEnum enumGroupName = DiscoveryDeviceGroupEnum.fromValue(groupName);
            DiscoverdDeviceResponse discoverdDeviceResponse = constructDiscoverdDeviceResponse(discoveredDeviceInfos, enumGroupName);
            discoverdDeviceResponseList.add(discoverdDeviceResponse);
        }
        return discoverdDeviceResponseList;
    }


    private DiscoverdDeviceResponse constructDiscoverdDeviceResponse(List<DiscoveredDeviceInfo> discoveredDeviceInfos, DiscoveryDeviceGroupEnum enumGroupName) {
        DiscoverdDeviceResponse discoverdDeviceResponse = new DiscoverdDeviceResponse();
        discoverdDeviceResponse.setDeviceGroup(enumGroupName.value());
        List<DiscoveredDeviceTypes> discoveredDeviceTypesList = new ArrayList<DiscoveredDeviceTypes>();
        for (String deviceName : discoveryDeviceConfigProvider.getAllDeviceTypeNameByGroup(enumGroupName)) {
            DiscoveredDeviceTypes discoveredDeviceTypes = new DiscoveredDeviceTypes();
            discoveredDeviceTypes.setDeviceName(deviceName);
            Collection<DiscoveredDeviceInfo> discoveredDeviceInfosListByDeviceName = CollectionUtils.select(discoveredDeviceInfos, predicateDeviceInfo(deviceName));
            discoveredDeviceTypes.setDiscovered(discoveredDeviceInfosListByDeviceName.size());
            discoveredDeviceTypes.setDiscoveredDeviceInfoList(new ArrayList<DiscoveredDeviceInfo>(discoveredDeviceInfosListByDeviceName));
            discoveredDeviceTypesList.add(discoveredDeviceTypes);
        }
        discoverdDeviceResponse.setDiscoveredDeviceTypesList(discoveredDeviceTypesList);
        return discoverdDeviceResponse;
    }


    private Predicate<DiscoveredDeviceInfo> predicateReachableUndiscoveredDevice() {
        return new Predicate<DiscoveredDeviceInfo>() {
            @Override
            public boolean evaluate(DiscoveredDeviceInfo deviceInfo) {
                if (deviceInfo == null) {
                    return false;
                }  
                return !Arrays.asList(DiscoveryDeviceStatus.NO_DEVICE.name(), DiscoveryDeviceStatus.DEVICE_IDENTFIED.name()).contains(deviceInfo.getStatus());
            }
        };
    }


    private Predicate<DiscoveredDeviceInfo> predicateDeviceInfo(String deviceName) {
        return new Predicate<DiscoveredDeviceInfo>() {
            @Override
            public boolean evaluate(DiscoveredDeviceInfo deviceInfo) {
                if (deviceInfo == null || deviceInfo.getDeviceType().equals(DiscoveryDeviceTypeEnum.UNKNOWN.value())) {
                    return false;
                }
                return deviceName.equals(deviceInfo.getDeviceType());
            }
        };
    }


    private Predicate<DiscoveredDeviceInfo> predicateDeviceInfos(List<String> devices) {
        return new Predicate<DiscoveredDeviceInfo>() {
            @Override
            public boolean evaluate(DiscoveredDeviceInfo deviceInfo) {
                if (deviceInfo == null || deviceInfo.getDeviceType().equals(DiscoveryDeviceTypeEnum.UNKNOWN.value())) {
                    return false;
                }
                return devices.contains(deviceInfo.getDeviceType());
            }
        };
    }


    private void overrideGlobalCredentials(Credential globalCredential) {
        String[] devices = Stream.of(DiscoveryDeviceTypeEnum.values()).map(DiscoveryDeviceTypeEnum::name).toArray(String[]::new);
        for (String device : devices) {
            if (device.equals(DiscoveryDeviceTypeEnum.UNKNOWN.value()))
                continue;
            requestScopeDiscoveryCredential.add(device, globalCredential);
        }

    }


    private void overrideRangeCredentials(Credential rangeCredential, String[] credentialGroupNames) {
        if (ArrayUtils.isEmpty(credentialGroupNames)) {
            requestScopeDiscoveryCredential.clear();
            credentialGroupNames = Stream.of(DiscoveryDeviceGroupEnum.values()).map(DiscoveryDeviceGroupEnum::name).toArray(String[]::new);
        }
        for (String credentialGroupName : credentialGroupNames) {
            if (EnumUtils.isValidEnum(DiscoveryDeviceGroupEnum.class, credentialGroupName)) {
                List<String> deviceTypeNmes = discoveryDeviceConfigProvider.getAllDeviceTypeNameByGroup(DiscoveryDeviceGroupEnum.valueOf(credentialGroupName));
                for (String name : deviceTypeNmes) {
                    requestScopeDiscoveryCredential.add(name.toUpperCase(), rangeCredential);
                }

            }
        }
    }


    private void overrideCredentials(Credential globalCredential, Credential rangeCredential, String[] credentialGroupNames) {
        requestScopeDiscoveryCredential.clear();
        if (globalCredential != null && !StringUtils.isEmpty(globalCredential.getUserName())) {
            overrideGlobalCredentials(globalCredential);
        }
        if (rangeCredential != null && !StringUtils.isEmpty(rangeCredential.getUserName())) {
            overrideRangeCredentials(rangeCredential, credentialGroupNames);
        }

        logger.trace("Override Credential : " + ReflectionToStringBuilder.toString(requestScopeDiscoveryCredential, new CustomRecursiveToStringStyle(99)));
    }
}
