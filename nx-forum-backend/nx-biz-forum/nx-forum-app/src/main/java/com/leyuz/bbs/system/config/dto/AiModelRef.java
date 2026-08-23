package com.leyuz.bbs.system.config.dto;

/**
 * 解析后的模型引用：厂商连接信息 + 具体模型 ID
 *
 * @param provider 厂商
 * @param model    模型 ID
 */
public record AiModelRef(AiModelProviderDTO provider, String model) {
}
