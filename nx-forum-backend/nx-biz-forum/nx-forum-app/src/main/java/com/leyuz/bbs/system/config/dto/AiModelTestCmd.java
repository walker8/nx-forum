package com.leyuz.bbs.system.config.dto;

import lombok.Data;

/**
 * 模型连通性测试命令
 *
 * @author Walker
 */
@Data
public class AiModelTestCmd {
    /**
     * 厂商 ID（apiKey 留空时用于服务端解析已保存的 Key）
     */
    private String providerId;

    /**
     * 模型地址（base URL）
     */
    private String apiUrl;

    /**
     * API Key（留空或为脱敏值则由服务端按 providerId 解析已保存的 Key）
     */
    private String apiKey = "";

    /**
     * 超时时间（秒）
     */
    private int timeoutSeconds = 60;

    /**
     * 模型 ID
     */
    private String model;
}
