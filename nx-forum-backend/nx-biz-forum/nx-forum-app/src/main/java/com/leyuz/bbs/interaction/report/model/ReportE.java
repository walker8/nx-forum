package com.leyuz.bbs.interaction.report.model;

import com.leyuz.bbs.interaction.report.dto.constant.ReportHandleStatusEnum;
import com.leyuz.bbs.interaction.report.dto.constant.ReportTargetTypeEnum;
import com.leyuz.bbs.interaction.report.dto.constant.ReportTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 举报实体模型
 */
@Data
public class ReportE {

    /**
     * 举报ID (主键)
     */
    private Long reportId;

    /**
     * 被举报内容ID (帖子ID或评论ID)
     */
    private Long targetId;

    /**
     * 举报目标类型 (1:主题, 2:评论 3:楼中楼回复)
     */
    private ReportTargetTypeEnum targetType;

    /**
     * 所属版块ID (冗余字段，便于查询)
     */
    private Integer forumId;

    /**
     * 举报原因类型
     */
    private ReportTypeEnum reportType;

    /**
     * 用户填写的补充说明
     */
    private String reportReason;

    /**
     * 被举报内容 (冗余字段)
     */
    private String reportedContent;

    /**
     * 处理状态 (0:待处理, 1:已处理-违规, 2:已处理-驳回)
     */
    private ReportHandleStatusEnum handleStatus;

    /**
     * 处理结果说明
     */
    private String handleReason;

    /**
     * 举报人IP地址
     */
    private String userIp;

    /**
     * 举报人客户端useragent
     */
    private String userAgent;

    /**
     * 举报人ID
     */
    private Long createBy;

    /**
     * 举报时间
     */
    private LocalDateTime createTime;

    /**
     * 处理人ID
     */
    private Long updateBy;

    /**
     * 处理时间
     */
    private LocalDateTime updateTime;

}

