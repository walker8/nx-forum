package com.leyuz.bbs.system.config.dto;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class AuditConfigSensitiveWordsDTO {
    // 是否开启敏感词审核
    private boolean enableSensitiveWordsAudit = true;
    // 敏感词列表（JSON数组格式存储）
    private Set<String> sensitiveWords = new HashSet<>();
} 