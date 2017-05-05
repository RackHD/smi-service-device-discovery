/**
 * Copyright © 2017 DELL Inc. or its subsidiaries.  All Rights Reserved.
 */
package com.dell.isg.smi.service.device.discovery.validation;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dell.isg.smi.commons.elm.exception.BusinessValidationException;
import com.dell.isg.smi.service.device.exception.EnumErrorCode;

public class ValidatedInet4Range implements Comparable<ValidatedInet4Range> {
    private static final Logger logger = LoggerFactory.getLogger(ValidatedInet4Range.class);
    private ValidatedInet4Address startAddress;


    public void setStartAddress(ValidatedInet4Address startAddress) {
        this.startAddress = startAddress;
    }

    private ValidatedInet4Address endAddress;


    public void setEndAddress(ValidatedInet4Address endAddress) {
        this.endAddress = endAddress;
    }


    public ValidatedInet4Range(ValidatedInet4Address address1, ValidatedInet4Address address2) {
        startAddress = address1;
        endAddress = address2;
        validateRangeEndpointOrder();
    }


    public ValidatedInet4Range(ValidatedInet4Address validatedInet4Address, int sizeOfRange) {
        if (0 >= sizeOfRange) {
            logger.warn("Validation failure: range size must be positive.");
            throw new BusinessValidationException(EnumErrorCode.INVALID_IPRANGE_SIZE_CODE);
        }
        startAddress = validatedInet4Address;
        endAddress = new ValidatedInet4Address(validatedInet4Address.getAddress() + (sizeOfRange - 1));
        validateRangeEndpointOrder();
    }


    private void validateRangeEndpointOrder() {
        if (endAddress.compareTo(startAddress) < 0) {
            logger.warn("Validation failure: Ending IP < Starting IP");
            throw new BusinessValidationException(EnumErrorCode.INVALID_IPRANGE_CODE);
        }
    }


    public ValidatedInet4Range(String startingIp, String endingIp) {
        this(new ValidatedInet4Address(startingIp), new ValidatedInet4Address(endingIp));
    }


    public boolean overlaps(ValidatedInet4Range other) {
        return this.startAddress.getAddress() <= other.endAddress.getAddress() && other.startAddress.getAddress() <= this.endAddress.getAddress();
    }


    public boolean isEntirelyWithinSubnet(ValidatedInet4SubnetMask mask) {
        return startAddress.getNetworkPrefix(mask).equals(endAddress.getNetworkPrefix(mask));
    }


    @Override
    public int hashCode() {
        return (startAddress.hashCode() ^ endAddress.hashCode());
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ValidatedInet4Range) {
            ValidatedInet4Range other = (ValidatedInet4Range) obj;
            return other.getStartAddress() == this.startAddress && other.getEndAddress() == this.endAddress;
        }
        return false;
    }


    public ValidatedInet4Address getEndAddress() {
        return endAddress;
    }


    public ValidatedInet4Address getStartAddress() {
        return startAddress;
    }


    public int compareTo(ValidatedInet4Range o2) {
        int result = startAddress.compareTo(o2.startAddress);
        if (0 == result) {
            result = endAddress.compareTo(o2.endAddress);
        }
        return result;
    }


    public List<String> getAddressStrings() {
        List<String> result = new ArrayList<String>();
        for (long addressValue = startAddress.getAddress(); addressValue <= endAddress.getAddress(); addressValue++) {
            result.add(Inet4ConverterValidator.convertIpValueToString(addressValue));
        }
        return result;
    }
}
