package com.leyuz.common.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Walker
 * @date 2024/1/6
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResultCode {
    public static final String SUCCESS_CODE = "0000";
    public static final String SERVER_ERROR_CODE = "0001";
    public static final String AUTH_FAILED_CODE = "0002";
    public static final String PERMISSION_DENIED_CODE = "0003";
    public static final String VALIDATION_ERROR_CODE = "0004";
    public static final String BUSINESS_ERROR_CODE = "0005";

    private String code;
    private String msg;
}
