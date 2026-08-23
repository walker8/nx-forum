package com.leyuz.bbs.system.config.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 共享 AI 模型库配置（厂商 → 模型）
 *
 * @author Walker
 */
@Data
public class AiModelConfigsDTO {
    /**
     * 厂商列表
     */
    private List<AiModelProviderDTO> providers = new ArrayList<>();
}
