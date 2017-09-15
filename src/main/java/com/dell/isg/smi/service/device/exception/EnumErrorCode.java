/**
 * Copyright © 2017 DELL Inc. or its subsidiaries.  All Rights Reserved.
 */
package com.dell.isg.smi.service.device.exception;

import com.dell.isg.smi.commons.elm.messaging.IMessageEnum;

public enum EnumErrorCode implements IMessageEnum {ENUM_INVALIDINPUT_ERROR("ENUM_INVALIDINPUT_ERROR", 234026),INVALID_NAME_LENGTH_CODE("INVALID_NAME_LENGTH_CODE",
			234001), INVALID_NAME_FORMAT_CODE("INVALID_NAME_FORMAT_CODE", 234002), INVALID_DESC_LENGTH_CODE(
					"INVALID_DESC_LENGTH_CODE", 234003), INVALID_DESC_FORMAT_CODE("INVALID_DESC_FORMAT_CODE",
							234004), INVALID_DISPLAY_NAME_LENGTH_CODE("INVALID_DISPLAY_NAME_LENGTH_CODE",
									234015), INVALID_DISPLAY_NAME_FORMAT_CODE("INVALID_DISPLAY_NAME_FORMAT_CODE",
											234016), INVALID_IPADDRESS_CODE("INVALID_IPADDRESS_CODE",
													234022), INVALID_IPRANGE_CODE("INVALID_IPRANGE_CODE",
															234024), INVALID_IPRANGE_SIZE_CODE(
																	"INVALID_IPRANGE_SIZE_CODE",
																	234025), ENUM_NOT_FOUND_ERROR(
																			"ENUM_NOT_FOUND_ERROR",
																			200404), ENUM_SERVER_ERROR(
																					"ENUM_SERVER_ERROR", 200500);

	private Integer id;
	private String value;

	private EnumErrorCode(String value, Integer id) {
		this.value = value;
		this.id = id;
	}

	public String getValue() {
		return this.value;
	}

	public Integer getId() {
		return this.id;
	}

	public String toString() {
		return this.value;
	}
}