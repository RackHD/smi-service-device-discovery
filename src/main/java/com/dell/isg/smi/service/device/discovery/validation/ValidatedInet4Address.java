/**
 * Copyright © 2017 DELL Inc. or its subsidiaries.  All Rights Reserved.
 */
package com.dell.isg.smi.service.device.discovery.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dell.isg.smi.commons.elm.exception.BusinessValidationException;
import com.dell.isg.smi.service.device.exception.EnumErrorCode;

public class ValidatedInet4Address implements Comparable<ValidatedInet4Address> {
    private static final Logger logger = LoggerFactory.getLogger(ValidatedInet4Address.class);
    private long address;


    public ValidatedInet4Address(String addressString) throws BusinessValidationException {
        address = Inet4ConverterValidator.convertIpStringToLong(addressString);
    }


    public ValidatedInet4Address(long value) throws BusinessValidationException {
        if (Inet4ConverterValidator.isValidIpAddress(value)) {
            address = value;
        } else {
            logger.debug("Invalid IP Address; entered IPAddress format is not correct");
            BusinessValidationException businessValidationException = new BusinessValidationException(EnumErrorCode.INVALID_IPADDRESS_CODE);
            businessValidationException.addAttribute(String.valueOf(value));
            throw businessValidationException;
        }
    }


    public long getAddress() {
        return address;
    }


    @Override
    public String toString() {
        return Inet4ConverterValidator.convertIpValueToString(address);
    }


    @Override
    public int compareTo(ValidatedInet4Address o) {
        if (this.getAddress() < o.getAddress()) {
            return -1;
        } else if (this.getAddress() == o.getAddress()) {
            return 0;
        }
        return 1;
    }


    @Override
    public boolean equals(Object o) {
        if (o instanceof ValidatedInet4Address) {
            ValidatedInet4Address other = (ValidatedInet4Address) o;
            return other.getAddress() == this.address;
        }
        return false;
    }


    @Override
    public int hashCode() {
        Long hasher = Long.valueOf(address);
        return hasher.hashCode();
    }


    public ValidatedInet4Address getNetworkPrefix(ValidatedInet4SubnetMask mask) {
        long prefix = address & mask.getValue();
        return new ValidatedInet4Address(prefix);
    }


    public ValidatedInet4Address getHostPart(ValidatedInet4SubnetMask mask) {
        long host = address & (~mask.getValue());
        return new ValidatedInet4Address(host);
    }
}
