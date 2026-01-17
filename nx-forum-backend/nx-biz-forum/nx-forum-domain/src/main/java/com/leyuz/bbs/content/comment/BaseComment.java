package com.leyuz.bbs.content.comment;

import com.leyuz.bbs.common.dataobject.AuditStatusV;
import com.leyuz.common.dto.UserClientInfo;
import com.leyuz.common.utils.HeaderUtils;
import com.leyuz.common.utils.UserAgentUtils;
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
     * 终端类型（PC/MOBILE/APP）
     */
    private String terminalType;

    /**
     * 平台（Windows/Mac/Android/iPhone等）
     */
    private String platform;

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

        // 简化：直接调用UserAgentUtils获取完整客户端信息
        String appVersion = HeaderUtils.getAppVersion();
        UserClientInfo clientInfo = UserAgentUtils.getClientInfo(userAgent, appVersion);
        terminalType = clientInfo.getTerminalType();
        platform = clientInfo.getPlatform();
    }

    protected abstract void checkMessage();

    public void setAuditResult(AuditStatusV auditStatus, String auditReason) {
        this.auditStatus = auditStatus;
        this.auditReason = auditReason;
    }
}
