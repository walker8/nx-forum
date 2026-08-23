package com.leyuz.bbs.system.audit.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台审核记录视图对象
 * <p>
 * 在 AuditLogE 基础上补充：用户名、版块名、操作人名、IP 地址等展示字段
 * </p>
 */
@Data
public class AuditLogVO {

    /**
     * 日志ID
     */
    private Long logId;

    /**
     * 内容类型 1=主贴 2=评论 3=楼中楼
     */
    private Integer contentType;

    /**
     * 内容类型名称（主贴/评论/楼中楼）
     */
    private String contentTypeName;

    /**
     * 内容ID（同步审核时可能为 NULL）
     */
    private Long contentId;

    /**
     * 版块ID
     */
    private Integer forumId;

    /**
     * 版块名称（昵称）
     */
    private String forumName;

    /**
     * 作者用户ID
     */
    private Long userId;

    /**
     * 作者用户名
     */
    private String authorName;

    /**
     * 作者IP
     */
    private String userIp;

    /**
     * 作者IP地理位置
     */
    private String ipLocation;

    /**
     * 审核会话ID
     */
    private String auditSessionId;

    /**
     * 触发来源 1=主动 2=编辑 3=举报 4=管理员后台
     */
    private Byte triggerSource;

    /**
     * 触发来源名称
     */
    private String triggerSourceName;

    /**
     * 审核阶段 1=黑白名单 2=敏感词 3=规则引擎 4=AI 5=人工
     */
    private Byte auditStage;

    /**
     * 审核阶段名称
     */
    private String auditStageName;

    /**
     * 审核前状态
     */
    private Byte auditStatusBefore;

    /**
     * 审核后状态
     */
    private Byte auditStatusAfter;

    /**
     * 审核后状态名称
     */
    private String auditStatusAfterName;

    /**
     * 命中详情JSON字符串
     */
    private String hitDetail;

    /**
     * 操作人ID（人工时为管理员）
     */
    private Long operatorId;

    /**
     * 操作人用户名（人工时为管理员）
     */
    private String operatorName;

    /**
     * 操作人类型 1=系统 2=管理员
     */
    private Byte operatorType;

    /**
     * 操作人类型名称
     */
    private String operatorTypeName;

    /**
     * 审核原因摘要
     */
    private String reason;

    /**
     * 审核耗时（毫秒）
     */
    private Integer costMs;

    /**
     * 内容快照
     */
    private String contentSnapshot;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
