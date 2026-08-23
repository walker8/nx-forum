package com.leyuz.bbs.system.audit;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.leyuz.common.dto.BaseEntity;
import lombok.Data;

import java.io.Serial;

/**
 * <p>
 * 内容审核记录持久化对象
 * </p>
 *
 * @author walker
 * @since 2026-08-19
 */
@TableName("bbs_audit_logs")
@Data
public class AuditLogPO extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    @TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;

    /**
     * 内容类型 1=主贴 2=评论 3=楼中楼
     */
    private Byte contentType;

    /**
     * 内容ID
     */
    private Long contentId;

    /**
     * 版块ID
     */
    private Integer forumId;

    /**
     * 内容作者ID
     */
    private Long userId;

    /**
     * 作者IP
     */
    private String userIp;

    /**
     * 审核会话ID（同步阶段 + AI 异步复审共享同一 session）
     */
    private String auditSessionId;

    /**
     * 触发来源 1=主动 2=编辑 3=举报 4=管理员后台
     */
    private Byte triggerSource;

    /**
     * 审核阶段 1=黑白名单 2=敏感词 3=规则引擎 4=AI 5=人工
     */
    private Byte auditStage;

    /**
     * 审核前状态 0通过 1审核中 2拒绝 3忽略
     */
    private Byte auditStatusBefore;

    /**
     * 审核后状态
     */
    private Byte auditStatusAfter;

    /**
     * 命中详情JSON字符串
     */
    private String hitDetail;

    /**
     * 操作人ID 系统=NULL 管理员=用户ID
     */
    private Long operatorId;

    /**
     * 操作人类型 1=系统 2=管理员
     */
    private Byte operatorType;

    /**
     * 审核原因摘要
     */
    private String reason;

    /**
     * 审核耗时（毫秒）
     */
    private Integer costMs;

    /**
     * 内容快照（便于内容被改/删后回溯）
     */
    private String contentSnapshot;
}
