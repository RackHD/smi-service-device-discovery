/**
 * Copyright © 2017 DELL Inc. or its subsidiaries.  All Rights Reserved.
 */
package com.dell.isg.smi.service.device.discovery.validation;

import com.dell.isg.smi.commons.elm.exception.BusinessValidationException;

public class ValidatedInet4SubnetMask {
    long maskValue;


    public ValidatedInet4SubnetMask(String mask) throws BusinessValidationException {
        maskValue = Inet4ConverterValidator.convertIpStringToLong(mask);
    }


    public long getValue() {
        return maskValue;
    }
}
