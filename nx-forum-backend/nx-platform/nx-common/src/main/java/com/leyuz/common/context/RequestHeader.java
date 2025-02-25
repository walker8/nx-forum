package com.leyuz.common.context;

import lombok.Data;

@Data
public class RequestHeader {
    /**
     * 用户id
     */
    private Long userId;
    /**
     * token
     */
    private String accessToken;
    /**
     * token
     */
    private String token;
    /**
     * 语言
     */
    private String lang;
    /**
     * user agent
     */
    private String userAgent;
    /**
     * ip地址
     */
    private String ip;
    /**
     * referer 来源
     */
    private String referer;
    /**
     * 设备id
     */
    private String deviceId;
    /**
     * 域名（带schema）
     */
    private String domain;
}
