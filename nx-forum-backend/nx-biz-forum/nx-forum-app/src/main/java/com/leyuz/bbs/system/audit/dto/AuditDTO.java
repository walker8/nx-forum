package com.leyuz.bbs.system.audit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuditDTO {
    private Long userId;
    private String userName;
    /**
     * 用于规则/AI 判定的纯文本（帖子为「标题 + 正文文本」，评论/回复为评论文本）
     */
    private String message;
    /**
     * 帖子标题（仅帖子场景，用于重复内容检测）
     */
    private String subject;
    /**
     * 原始内容（仅帖子场景为 HTML，用于外链检测）
     */
    private String content;
    /**
     * 内容类型
     */
    private AuditContentType contentType;
    /**
     * 版块 ID
     */
    private Integer forumId;
    /**
     * 是否新建（false 为编辑）
     */
    private Boolean isNew;
}
