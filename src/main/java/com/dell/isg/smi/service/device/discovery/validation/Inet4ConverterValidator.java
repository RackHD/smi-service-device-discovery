/**
 * Copyright © 2017 DELL Inc. or its subsidiaries.  All Rights Reserved.
 */
package com.dell.isg.smi.service.device.discovery.validation;

import java.util.BitSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dell.isg.smi.commons.elm.exception.BusinessValidationException;
import com.dell.isg.smi.service.device.exception.EnumErrorCode;

public final class Inet4ConverterValidator {
    private Inet4ConverterValidator() {

    }

    public static final long MAX_IP_ADDRESS = 0x00000000FFFFFFFFl;
    public static final long MIN_IP_ADDRESS = 0x0000000000000000l;
    private static final Logger logger = LoggerFactory.getLogger(Inet4ConverterValidator.class);
    private static final String IPADDRESS_PATTERN = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
    private static Pattern pattern;


    public static long convertIpStringToLong(String address) {
        if (StringUtils.isBlank(address)) {
            logger.debug("IP conversion failed: entered IP Address string is null or empty.");
            BusinessValidationException bve = new BusinessValidationException(EnumErrorCode.INVALID_IPADDRESS_CODE);
            bve.addAttribute(address);
            throw bve;
        }
        pattern = Pattern.compile(IPADDRESS_PATTERN);
        Matcher ipAddressMatch = pattern.matcher(address);
        if (!ipAddressMatch.matches()) {
            logger.debug("Invalid IP Address; entered IPAddress format is not correct");
            BusinessValidationException bve = new BusinessValidationException(EnumErrorCode.INVALID_IPADDRESS_CODE);
            bve.addAttribute(address);
            throw bve;
        }

        long result = 0;
        String[] parts = address.split("\\.");
        for (String part : parts) {
            try {
                int partValue = Integer.parseInt(part);
                result = result * 0x100 + partValue;
            } catch (NumberFormatException e) {
                if (logger.isDebugEnabled())
                    logger.debug("IP conversion failed: integer parse of part threw NumberFormatException (IP part not numeric)");
                throw new BusinessValidationException(EnumErrorCode.ENUM_INVALIDINPUT_ERROR);
            }
        }
        return result;
    }


    public static String convertIpValueToString(long address) {
        long aField = ((address & 0x00000000FF000000l) >>> 24);
        long bField = ((address & 0x0000000000FF0000l) >>> 16);
        long cField = ((address & 0x000000000000FF00l) >>> 8);
        long dField = (address & 0x00000000000000FFl);
        return String.format("%1$1d.%2$1d.%3$1d.%4$1d", aField, bField, cField, dField);
    }


    public static boolean isValidSubnetMask(long maskValue) {
        BitSet maskBits = getRightmost32BitsFromlLong(maskValue);
        // leftmost bits (at least one) must be 1
        // rightmost bits (at least one) must be 0
        if (maskBits.get(0) || !maskBits.get(31)) {
            return false;
        }
        // find rightmost set bit, ensure all bits left of that (up to index 31) are set.
        int n = maskBits.nextSetBit(0);
        return (32 == maskBits.nextClearBit(n));
    }


    private static BitSet getRightmost32BitsFromlLong(long value) {
        logger.trace("getRightmost32BitsFromlLong(" + value + ")");
        final int size = 32;
        BitSet bits = new BitSet(size);

        for (int index = 0; index < size; index++) {
            long mask = 0x01L << index;
            if (0 < (mask & value)) {
                bits.set(index);
            }
        }

        logger.trace("getRightmost32BitsFromlLong returning " + bits);
        return bits;
    }


    public static boolean isValidIpAddress(long value) {
        return (MIN_IP_ADDRESS <= value) && (value <= MAX_IP_ADDRESS);
    }


    public static boolean isValidIpAddress(String addressString) {
        long ipValue = convertIpStringToLong(addressString);
        return isValidIpAddress(ipValue);
    }

}
