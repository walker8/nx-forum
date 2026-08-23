package com.leyuz.bbs.system.config.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 厂商（连接信息），同一厂商下可配置多个模型 ID
 *
 * @author Walker
 */
@Data
public class AiModelProviderDTO {
    /**
     * 厂商唯一标识
     */
    private String id;

    /**
     * 厂商名称
     */
    private String name;

    /**
     * 模型地址（base URL，客户端自动拼接 /chat/completions）
     */
    private String apiUrl;

    /**
     * API Key（本地 Ollama 可留空）
     */
    private String apiKey = "";

    /**
     * 超时时间（秒）
     */
    private int timeoutSeconds = 60;

    /**
     * 是否启用
     */
    private boolean enabled = true;

    /**
     * 该厂商下的模型 ID 列表
     */
    private List<AiModelDTO> models = new ArrayList<>();
}
