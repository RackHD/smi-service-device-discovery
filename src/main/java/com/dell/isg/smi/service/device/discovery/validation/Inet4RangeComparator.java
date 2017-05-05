/**
 * Copyright © 2017 DELL Inc. or its subsidiaries.  All Rights Reserved.
 */
package com.dell.isg.smi.service.device.discovery.validation;

import java.io.Serializable;
import java.util.Comparator;

public class Inet4RangeComparator implements Comparator<ValidatedInet4Range>, Serializable {

    private static final long serialVersionUID = 1L;


    @Override
    public int compare(ValidatedInet4Range o1, ValidatedInet4Range o2) {
        return o1.compareTo(o2);
    }

}
