package com.leyuz.bbs.interaction.report.dto;

import com.leyuz.bbs.interaction.report.dto.constant.ReportHandleStatusEnum;
import com.leyuz.bbs.interaction.report.dto.constant.ReportTargetTypeEnum;
import com.leyuz.common.mybatis.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 举报分页查询
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ReportPageQuery extends PageQuery {

    @Serial
    private static final long serialVersionUID = 4161621480357041823L;

    /**
     * 举报目标类型 (1:主题, 2:评论 3:楼中楼回复)
     */
    private ReportTargetTypeEnum targetType;

    /**
     * 所属版块ID
     */
    private Integer forumId;

    /**
     * 处理状态 (0:待处理, 1:已处理-违规, 2:已处理-驳回)
     */
    private ReportHandleStatusEnum handleStatus;

    /**
     * 举报人ID
     */
    private Long reporterId;

    /**
     * 处理人ID
     */
    private Long handlerId;

}

