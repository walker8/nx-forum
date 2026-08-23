package com.leyuz.bbs.system.audit.domain;

import com.leyuz.bbs.common.dataobject.AuditStageV;
import com.leyuz.bbs.common.dataobject.AuditStatusV;
import com.leyuz.bbs.common.dataobject.AuditTriggerSourceV;
import com.leyuz.bbs.common.dataobject.OperatorTypeV;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 内容审核记录实体
 * <p>
 * 记录每一次审核事件：内容维度 + 作者维度 + 审核阶段 + 命中详情 + 操作人 + 结果
 * </p>
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuditLogE {

    /**
     * 日志ID
     */
    private Long logId;

    /**
     * 内容类型 1=主贴 2=评论 3=楼中楼
     */
    private Integer contentType;

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
     * 触发来源
     */
    private AuditTriggerSourceV triggerSource;

    /**
     * 审核阶段
     */
    private AuditStageV auditStage;

    /**
     * 审核前状态
     */
    private AuditStatusV auditStatusBefore;

    /**
     * 审核后状态
     */
    private AuditStatusV auditStatusAfter;

    /**
     * 命中详情JSON字符串（按 auditStage 区分结构）
     */
    private String hitDetail;

    /**
     * 操作人ID：系统自动时为 null，管理员人工时为用户ID
     */
    private Long operatorId;

    /**
     * 操作人类型
     */
    private OperatorTypeV operatorType;

    /**
     * 审核原因摘要
     */
    private String reason;

    /**
     * 审核耗时（毫秒）
     */
    private Integer costMs;

    /**
     * 内容快照（便于内容被改/删后回溯，截断 2000 字）
     */
    private String contentSnapshot;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
