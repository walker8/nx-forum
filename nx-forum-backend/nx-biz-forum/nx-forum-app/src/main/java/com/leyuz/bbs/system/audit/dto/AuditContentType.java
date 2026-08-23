package com.leyuz.bbs.system.audit.dto;

/**
 * 审核内容类型
 *
 * @author Walker
 */
public enum AuditContentType {
    /**
     * 帖子
     */
    THREAD,

    /**
     * 评论
     */
    COMMENT,

    /**
     * 楼中楼回复
     */
    REPLY
}
