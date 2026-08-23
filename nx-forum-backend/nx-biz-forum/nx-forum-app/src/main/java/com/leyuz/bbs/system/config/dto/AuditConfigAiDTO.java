package com.leyuz.bbs.system.config.dto;

import lombok.Data;

/**
 * AI 审核场景配置（模型引用共享模型库，不内嵌模型连接信息）
 *
 * @author Walker
 */
@Data
public class AuditConfigAiDTO {
    /**
     * 是否启用 AI 审核
     */
    private boolean enabled = false;

    /**
     * 当前生效的模型配置 ID（引用共享模型库）
     */
    private String activeModelId;

    /**
     * 历史发帖/回帖上下文的条数（每种内容类型各取 N 条）
     */
    private int historyCount = 5;

    /**
     * 审核场景系统提示词（要求模型输出 JSON verdict）
     */
    private String prompt = "你是论坛内容安全审核助手。请根据给定内容及其上下文（发帖人信息、发帖信息、历史发帖/回帖）判断内容是否违规。判断维度：违法违规、色情低俗、暴力恐怖、人身攻击辱骂、广告营销垃圾信息、政治敏感等。请只输出一个 JSON 对象（不要输出任何其他文字），格式：{\"verdict\":\"PASS|REVIEW|REJECT\",\"reason\":\"详细理由（仅供管理员审计，可包含内部推理细节）\",\"userReason\":\"面向用户的简短违规说明（不超过 50 字，只描述违规事实，不要暴露判断规则、历史行为、推理过程）\",\"confidence\":0.0~1.0}。verdict 含义：PASS=正常放行，REVIEW=存疑转人工复审，REJECT=明显违规。";
}
