package com.leyuz.bbs.system.access;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 访问日志实体类
 *
 * @author Walker
 * @since 2025-01-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("bbs_access_logs")
public class AccessLogPO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    @TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;

    /**
     * 访问时间
     */
    private LocalDateTime accessTime;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 设备ID（前端传值，Web端为空）
     */
    private String deviceId;

    /**
     * App版本号（Web端为空）
     */
    private String appVersion;

    /**
     * 用户ID（0表示未登录）
     */
    private Long userId;

    /**
     * User-Agent原始值
     */
    private String userAgent;

    /**
     * 终端类型（PC/MOBILE/APP）
     */
    private String terminalType;

    /**
     * 平台名称（Windows/Android/iOS等）
     */
    private String platform;

    /**
     * 浏览器名称（Chrome/Safari等）
     */
    private String browserName;
}
