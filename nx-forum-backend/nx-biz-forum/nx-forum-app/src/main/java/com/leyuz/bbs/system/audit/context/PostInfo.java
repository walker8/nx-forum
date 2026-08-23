package com.leyuz.bbs.system.audit.context;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 发帖信息（规则/AI 入参）
 *
 * @author Walker
 */
@Getter
@Builder
public class PostInfo {
    /**
     * 是否新建（false 为编辑）
     */
    private Boolean isNew;
    /**
     * 内容类型：THREAD / COMMENT / REPLY
     */
    private String contentType;
    /**
     * 版块 ID
     */
    private Integer forumId;
    /**
     * 版块名称
     */
    private String forumName;
    /**
     * 标题（评论/回复为空）
     */
    private String subject;
    /**
     * 发帖 IP
     */
    private String ip;
    /**
     * 发帖地址（IP 归属地）
     */
    private String ipLocation;
    /**
     * 发帖时间
     */
    private LocalDateTime createTime;
    /**
     * 发帖小时（0-23）
     */
    private int hour;
}
