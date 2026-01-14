package com.leyuz.common.dto;

import lombok.Data;

/**
 * 客户端基本信息
 */
@Data
public class UserClientInfo {
    private String os;
    private String platform;
    private String browser;
}
