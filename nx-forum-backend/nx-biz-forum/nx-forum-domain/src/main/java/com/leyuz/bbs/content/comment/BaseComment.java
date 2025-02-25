package com.leyuz.bbs.content.comment;

import com.leyuz.bbs.common.dataobject.AuditStatusV;
import com.leyuz.common.utils.HeaderUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseComment {
    private Long threadId;

    @Setter
    private Long commentId;

    /**
     * 版块ID
     */
    private Integer forumId;

    /**
     * 发帖时用户ip地址
     */
    private String userIp;

    /**
     * 客户端useragent
     */
    private String userAgent;

    /**
     * 点赞数
     */
    private Integer likes;

    /**
     * 0 审核通过 1 审核中 2 审核拒绝
     */
    private AuditStatusV auditStatus;

    /**
     * 审核原因
     */
    private String auditReason;

    /**
     * 内容
     */
    protected String message;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @Setter
    private Long createBy;

    private Long updateBy;

    public void create() {
        checkMessage();
        userIp = HeaderUtils.getIp();
        userAgent = HeaderUtils.getUserAgent();
    }

    protected abstract void checkMessage();

    public void setAuditResult(AuditStatusV auditStatus, String auditReason) {
        this.auditStatus = auditStatus;
        this.auditReason = auditReason;
    }
}
