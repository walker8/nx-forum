package com.leyuz.uc.user.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户事件，用于记录用户相关操作日志
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginEvent {
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 日志类型
     */
    private Integer logType;

    /**
     * 日志内容
     */
    private String logContent;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 操作状态
     */
    private Integer operationStatus;
} 