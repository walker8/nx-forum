package com.leyuz.bbs.interaction.report.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.leyuz.bbs.interaction.report.dto.constant.ReportHandleStatusEnum;
import com.leyuz.bbs.interaction.report.dto.constant.ReportTargetTypeEnum;
import com.leyuz.bbs.interaction.report.dto.constant.ReportTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(name = "举报信息")
public class ReportDTO {

    @Schema(description = "举报ID")
    private Long reportId;

    @Schema(description = "被举报内容ID")
    private Long targetId;

    @Schema(description = "被举报内容")
    private String reportedContent;

    @Schema(description = "举报目标类型")
    private ReportTargetTypeEnum targetType;

    @Schema(description = "所属版块ID")
    private Integer forumId;

    @Schema(description = "举报原因类型")
    private ReportTypeEnum reportType;

    @Schema(description = "用户填写的补充说明")
    private String reportReason;

    @Schema(description = "处理状态")
    private ReportHandleStatusEnum handleStatus;

    @Schema(description = "处理结果说明")
    private String handleReason;

    @Schema(description = "举报人")
    private String reporterName;

    @Schema(description = "举报时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createTime;

    @Schema(description = "处理人")
    private String handlerName;

    @Schema(description = "处理时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime updateTime;

    @Schema(description = "转跳地址")
    private String redirectUrl;
} 