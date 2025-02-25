package com.leyuz.bbs.interaction.report.dto.command;

import com.leyuz.bbs.interaction.report.dto.constant.ReportTargetTypeEnum;
import com.leyuz.bbs.interaction.report.dto.constant.ReportTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(name = "创建举报命令")
public class ReportCreateCmd {

    @NotNull
    @Schema(description = "被举报内容ID (帖子ID或评论ID)")
    private Long targetId;

    @NotNull
    @Schema(description = "举报目标类型 (1:主题, 2:评论 3:楼中楼回复)")
    private ReportTargetTypeEnum targetType;

    @NotNull
    @Schema(description = "所属版块ID")
    private Integer forumId;

    @NotNull
    @Schema(description = "举报原因类型")
    private ReportTypeEnum reportType;

    @Schema(description = "用户填写的补充说明")
    private String reportReason;

} 