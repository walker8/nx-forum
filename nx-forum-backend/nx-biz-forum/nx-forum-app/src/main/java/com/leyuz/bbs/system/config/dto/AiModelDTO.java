package com.leyuz.bbs.system.config.dto;

import lombok.Data;

/**
 * 厂商下的单个模型（模型 ID）
 *
 * @author Walker
 */
@Data
public class AiModelDTO {
    /**
     * 模型唯一键（供业务场景引用）
     */
    private String id;

    /**
     * 模型 ID（如 Minimax-M3、deepseek-chat）
     */
    private String model;
}
