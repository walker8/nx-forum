package com.leyuz.bbs.interaction.report;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.leyuz.common.dto.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 举报持久化对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bbs_reports")
public class ReportPO extends BaseEntity {

    /**
     * 举报ID (主键)
     */
    @TableId(value = "report_id", type = IdType.AUTO)
    private Long reportId;

    /**
     * 被举报内容ID (帖子ID或评论ID)
     */
    private Long targetId;

    /**
     * 举报目标类型 (1:主题, 2:评论 3:楼中楼回复)
     */
    private Integer targetType;

    /**
     * 所属版块ID (冗余字段，便于查询)
     */
    private Integer forumId;

    /**
     * 举报原因类型
     */
    private Integer reportType;

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
    private Integer handleStatus;

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
} 