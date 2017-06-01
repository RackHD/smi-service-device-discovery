
package com.dell.isg.smi.service.device.discovery.ui.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.IntStream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dell.isg.smi.commons.model.device.discovery.DiscoveryDeviceGroupEnum;
import com.dell.isg.smi.commons.model.device.discovery.config.DeviceDefaultCredential;
import com.dell.isg.smi.commons.model.device.discovery.config.DeviceType;
import com.dell.isg.smi.commons.model.device.discovery.config.Identifier;
import com.dell.isg.smi.service.device.discovery.config.DiscoveryDeviceConfig;
import com.dell.isg.smi.service.device.discovery.config.DiscoveryDeviceConfigProvider;

@Component
public class ConfigDeviceTypeHandler {

	@Autowired
	DiscoveryDeviceConfig discoveryDeviceConfig;

	@Autowired
	DiscoveryDeviceConfigProvider discoveryDeviceConfigProvider;

	CopyOnWriteArrayList<ConfigDeviceType> configDeviceTypeMasterList = new CopyOnWriteArrayList<ConfigDeviceType>();
	
	void loadDeviceTypes(){
		for (DiscoveryDeviceGroupEnum deviceGroupEnum : DiscoveryDeviceGroupEnum.values()) {
			List<DeviceType> deviceTypeList = discoveryDeviceConfigProvider.getDeviceTypeByGroup(deviceGroupEnum);
			configDeviceTypeMasterList.addAll(transform(deviceGroupEnum.name(), deviceTypeList));
		}
	}
	
	List<ConfigDeviceType> findAll() {
		if (CollectionUtils.isEmpty(configDeviceTypeMasterList)) {
			loadDeviceTypes();
		}
		return configDeviceTypeMasterList;
	}

	private List<ConfigDeviceType> transform(String name, List<DeviceType> deviceTypeList) {
		List<ConfigDeviceType> configDeviceTypeList = new ArrayList<ConfigDeviceType>();
		if (CollectionUtils.isEmpty(deviceTypeList)) {
			return configDeviceTypeList;
		}
		for (DeviceType deviceType : deviceTypeList) {
			ConfigDeviceType configDeviceType = new ConfigDeviceType(name);
			BeanUtils.copyProperties(deviceType, configDeviceType);
			configDeviceType.setUsername(deviceType.getDeviceDefaultCredential().getUsername());
			configDeviceType.setPassword(deviceType.getDeviceDefaultCredential().getPassword());
			List<String> identifierStrList = new ArrayList<String>();
			for (Identifier identifier : deviceType.getIdentifier()) {
				identifierStrList.add(identifier.getText());
			}
			String[] identifiers = identifierStrList.stream().toArray(String[]::new);
			StringBuilder sb = new StringBuilder();
			int count = 1;
			for (String s : identifiers)
			{
			    sb.append(count +" : "+s + "\n");
			    count++;
			}
			configDeviceType.setIdentifiers(sb.toString());
			configDeviceTypeList.add(configDeviceType);
		}
		return configDeviceTypeList;
	}

	List<ConfigDeviceType> findByNameLikeIgnoreCase(String nameFilter) {
		if (CollectionUtils.isEmpty(configDeviceTypeMasterList)) {
			loadDeviceTypes();
		}
		List<ConfigDeviceType> convertedList = Arrays.asList(configDeviceTypeMasterList.stream().toArray(ConfigDeviceType[]::new));	
		List<ConfigDeviceType> configDeviceTypeList = convertedList;
		if (!StringUtils.isEmpty(nameFilter)){
			configDeviceTypeList = (List<ConfigDeviceType>) CollectionUtils.select(convertedList , predicateConfigDeviceType(nameFilter.toUpperCase()));
		}
		return configDeviceTypeList;
	}
	
	private Predicate<ConfigDeviceType> predicateConfigDeviceType(String filterString) {
		return new Predicate<ConfigDeviceType>() {
			@Override
			public boolean evaluate(ConfigDeviceType configDeviceType) {
				if (configDeviceType == null) {
					return false;
				}
				return configDeviceType.getGroup().contains(filterString);
			}
		};
	}

	synchronized void save(ConfigDeviceType configDeviceType) {
		OptionalInt indexOpt = IntStream.range(0, configDeviceTypeMasterList.size())
			     .filter(i -> configDeviceType.getName().equals(configDeviceTypeMasterList.get(i).getName()))
			     .findFirst();
		configDeviceTypeMasterList.set(indexOpt.getAsInt(), configDeviceType);
		DeviceType deviceType = new DeviceType();
		deviceType.setName(configDeviceType.getName());
		deviceType.setDeviceDefaultCredential(new DeviceDefaultCredential(configDeviceType.getUsername(),configDeviceType.getPassword()));
		deviceType.setEnabled(configDeviceType.getEnabled());
		discoveryDeviceConfigProvider.modifyDeviceTypeConfig(deviceType,configDeviceType.getGroup());
	}

	void reset() {
		discoveryDeviceConfigProvider.restore();
		configDeviceTypeMasterList.clear();
		loadDeviceTypes();
	}
	
	void loadFrom(DiscoveryDeviceConfig deviceConfig) {
		discoveryDeviceConfigProvider.loadFrom(deviceConfig);
		configDeviceTypeMasterList.clear();
		loadDeviceTypes();
	}

}
