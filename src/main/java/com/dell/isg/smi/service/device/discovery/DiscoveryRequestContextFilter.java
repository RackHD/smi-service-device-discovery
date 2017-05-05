/**
 * Copyright © 2017 DELL Inc. or its subsidiaries.  All Rights Reserved.
 */
package com.dell.isg.smi.service.device.discovery;

import javax.servlet.annotation.WebFilter;

import org.springframework.web.filter.RequestContextFilter;

@WebFilter
public class DiscoveryRequestContextFilter extends RequestContextFilter {

}
