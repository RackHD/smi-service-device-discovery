/**
 * Copyright © 2017 DELL Inc. or its subsidiaries.  All Rights Reserved.
 */
package com.dell.isg.smi.service.device.discovery.validation;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dell.isg.smi.commons.elm.exception.InvalidArgumentsException;
import com.dell.isg.smi.commons.model.device.discovery.DiscoverDeviceRequest;

public class IPRangeValidatorUtil {

    private static final Logger logger = LoggerFactory.getLogger(IPRangeValidatorUtil.class.getName());


    /**
     * Expand the IP range and validate
     *
     * @param discoverIpRange DiscoverIPRangeDeviceRequest
     * @return list of ips or error
     */
    public static List<String> expandIpAddresses(DiscoverDeviceRequest discoverIpRange) throws Exception {
        List<String> ipAddresslist = new ArrayList<>();

        if (discoverIpRange.getDeviceStartIp() == null || discoverIpRange.getDeviceStartIp().isEmpty()) {
            String msg = "IP or range";
            throw new InvalidArgumentsException(msg);
        } else if (discoverIpRange.getDeviceEndIp() == null || discoverIpRange.getDeviceEndIp().isEmpty()) {
            // just add the first IP
            new ValidatedInet4Address(discoverIpRange.getDeviceStartIp());
            ipAddresslist.add(discoverIpRange.getDeviceStartIp());
        } else {

            // First check if IPs first 3 parts are same
            validateIpSameSubnet(discoverIpRange.getDeviceStartIp(), discoverIpRange.getDeviceEndIp());

            // we have the range specified
            ValidatedInet4Range validatedRange = new ValidatedInet4Range(discoverIpRange.getDeviceStartIp(), discoverIpRange.getDeviceEndIp());
            List<String> addressStrings = validatedRange.getAddressStrings();
            for (String address : addressStrings) {
                ipAddresslist.add(address);
            }
        }
        return ipAddresslist;
    }


    public static boolean validateIpAddress(String ipAddress) {
        if (ipAddress == null || ipAddress.isEmpty()) {
            String msg = "IP";
            throw new InvalidArgumentsException(msg);
        } else {
            new ValidatedInet4Address(ipAddress);
            return true;
        }
    }


    public static void validateIpSameSubnet(String ip1, String ip2) throws IllegalArgumentException {
        if ((ip1 != null && !ip1.isEmpty()) && (ip2 != null && !ip2.isEmpty())) {
            if (!ip1.substring(0, ip1.lastIndexOf(".")).equalsIgnoreCase(ip2.substring(0, ip2.lastIndexOf(".")))) {
                throw new InvalidArgumentsException(" IP range: IP not on the same subnet");
            }
        }
    }


    public static boolean ping(String ipAddress) {
        try {
            return InetAddress.getByName(ipAddress).isReachable(2000);
        } catch (Exception e) {
            return false;
        }
    }


    public static String getMacAddressForIp(String ipAddress, String command) {
        String cmd = String.format(command, ipAddress);
        String macAddress = "";
        try {
            Scanner scanner = new Scanner(Runtime.getRuntime().exec(cmd).getInputStream());
            scanner.useDelimiter("\\A");
            StringBuffer buffer = new StringBuffer();
            try {
                while (scanner.hasNext()) {
                    buffer.append(scanner.next());
                }
            } finally {
                scanner.close();
            }
            macAddress = StringUtils.substringBetween(buffer.toString(), "bytes from", String.format(" (%s)", ipAddress));
        } catch (Exception e) {
            logger.error("Cannot get the MacAddress for IP :" + ipAddress);
        }
        return macAddress;
    }
}
