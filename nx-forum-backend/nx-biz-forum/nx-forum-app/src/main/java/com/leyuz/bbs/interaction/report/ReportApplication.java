package com.leyuz.bbs.interaction.report;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyuz.bbs.auth.ForumPermissionResolver;
import com.leyuz.bbs.content.comment.CommentApplication;
import com.leyuz.bbs.content.comment.CommentE;
import com.leyuz.bbs.content.comment.CommentReplyE;
import com.leyuz.bbs.content.comment.gateway.CommentGateway;
import com.leyuz.bbs.content.thread.ThreadApplication;
import com.leyuz.bbs.content.thread.ThreadE;
import com.leyuz.bbs.content.thread.gateway.ThreadGateway;
import com.leyuz.bbs.interaction.report.dto.ReportDTO;
import com.leyuz.bbs.interaction.report.dto.ReportPageQuery;
import com.leyuz.bbs.interaction.report.dto.command.ReportCreateCmd;
import com.leyuz.bbs.interaction.report.dto.command.ReportHandleCmd;
import com.leyuz.bbs.interaction.report.dto.constant.ReportHandleStatusEnum;
import com.leyuz.bbs.interaction.report.dto.constant.ReportTargetTypeEnum;
import com.leyuz.bbs.interaction.report.dto.constant.ReportTypeEnum;
import com.leyuz.bbs.interaction.report.model.ReportE;
import com.leyuz.bbs.system.notification.NotificationApplication;
import com.leyuz.common.exception.ValidationException;
import com.leyuz.common.mybatis.CustomPage;
import com.leyuz.common.mybatis.DataBaseUtils;
import com.leyuz.common.utils.HeaderUtils;
import com.leyuz.uc.user.UserApplication;
import com.leyuz.uc.user.UserE;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReportApplication {

    private final ReportMapper reportMapper;
    private final ThreadGateway threadGateway;
    private final ThreadApplication threadApplication;
    private final CommentGateway commentGateway;
    private final CommentApplication commentApplication;
    private final UserApplication userApplication;
    private final ForumPermissionResolver forumPermissionResolver;
    private final NotificationApplication notificationApplication;

    public void createReport(ReportCreateCmd cmd) {
        // 禁止重复举报校验
        Long userId = HeaderUtils.getUserId();
        LambdaQueryWrapper<ReportPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReportPO::getCreateBy, userId)
                .eq(ReportPO::getTargetId, cmd.getTargetId())
                .eq(ReportPO::getTargetType, cmd.getTargetType().getValue())
                .eq(ReportPO::getHandleStatus, ReportHandleStatusEnum.PENDING.getValue());
        boolean duplicate = reportMapper.selectCount(wrapper) > 0;
        if (duplicate) {
            throw new ValidationException("请勿重复举报，待处理举报已存在");
        }
        ReportE report = new ReportE();
        report.setReportReason(cmd.getReportReason());
        report.setTargetId(cmd.getTargetId());
        ReportTargetTypeEnum targetType = cmd.getTargetType();
        report.setTargetType(targetType);
        report.setForumId(cmd.getForumId());
        report.setReportType(cmd.getReportType());
        String reportedContent = getReportedContent(cmd, targetType);
        report.setReportedContent(reportedContent);
        report.setHandleStatus(ReportHandleStatusEnum.PENDING);
        report.setCreateBy(HeaderUtils.getUserId());
        report.setUserIp(HeaderUtils.getIp());
        report.setUserAgent(HeaderUtils.getUserAgent());
        report.setCreateTime(LocalDateTime.now());
        report.setUpdateTime(report.getCreateTime());
        report.setUpdateBy(report.getCreateBy());

        ReportPO po = entityToPo(report);
        reportMapper.insert(po);
    }

    private String getReportedContent(ReportCreateCmd cmd, ReportTargetTypeEnum targetType) {
        String reportedContent = "";
        switch (targetType) {
            case THREAD:
                ThreadE thread = threadGateway.getThread(cmd.getTargetId());
                if (thread == null) {
                    throw new ValidationException("主题不存在");
                }
                reportedContent = StringUtils.isNotEmpty(thread.getSubject()) ? thread.getSubject() : thread.getBrief();
                break;
            case COMMENT:
                CommentE comment = commentGateway.getComment(cmd.getTargetId());
                if (comment == null) {
                    throw new ValidationException("评论不存在");
                }
                reportedContent = comment.getMessage();
                break;
            case REPLY:
                CommentReplyE commentReply = commentGateway.getCommentReply(cmd.getTargetId());
                if (commentReply == null) {
                    throw new ValidationException("回复不存在");
                }
                reportedContent = commentReply.getMessage();
                break;
        }
        // 截取前80个字符，超出的部分用...代替
        reportedContent = StringUtils.abbreviate(reportedContent, 80);
        return reportedContent;
    }

    public void handleReport(ReportHandleCmd cmd) {
        ReportPO reportPO = reportMapper.selectById(cmd.getReportId());
        if (reportPO == null) {
            throw new ValidationException("举报不存在");
        }
        ReportE report = poToEntity(reportPO);
        forumPermissionResolver.checkPermission(report.getForumId(), "admin:user:report");
        if (cmd.getHandleStatus() == ReportHandleStatusEnum.PENDING) {
            throw new ValidationException("处理状态不能为待处理");
        }
        report.setHandleStatus(cmd.getHandleStatus());
        report.setHandleReason(cmd.getHandleReason());
        report.setUpdateBy(HeaderUtils.getUserId());
        report.setUpdateTime(LocalDateTime.now());
        if (ReportHandleStatusEnum.APPROVED.equals(cmd.getHandleStatus())) {
            String reason = report.getHandleReason();
            switch (report.getTargetType()) {
                case THREAD:
                    threadApplication.deleteThread(report.getTargetId(), reason, cmd.getNotifyUser());
                    break;
                case COMMENT:
                    commentApplication.deleteComment(report.getTargetId(), reason, cmd.getNotifyUser());
                    break;
                case REPLY:
                    commentApplication.deleteCommentReply(report.getTargetId(), reason, cmd.getNotifyUser());
                    break;
            }
        }
        ReportPO updatedPO = entityToPo(report);
        reportMapper.updateById(updatedPO);

        // 如果需要通知用户
        if (Boolean.TRUE.equals(cmd.getNotifyUser())) {
            sendNotificationToReporter(report);
        }
    }

    /**
     * 向举报人发送系统通知
     *
     * @param report 举报实体
     */
    private void sendNotificationToReporter(ReportE report) {
        String status = report.getHandleStatus() == ReportHandleStatusEnum.APPROVED ? "违规" : "驳回";
        String subject = "您的举报已处理";

        // 构建通知内容
        StringBuilder message = new StringBuilder();
        message.append("您举报的内容已被管理员处理为【").append(status).append("】。<br />");

        // 添加被举报内容摘要
        if (StringUtils.isNotEmpty(report.getReportedContent())) {
            message.append("被举报内容: ").append(report.getReportedContent()).append("<br />");
        }

        // 添加处理说明（如果有）
        if (report.getHandleStatus() == ReportHandleStatusEnum.REJECTED
                && StringUtils.isNotEmpty(report.getHandleReason())) {
            // 驳回原因
            message.append("处理说明: ").append(report.getHandleReason());
        }

        // 发送系统通知
        notificationApplication.sendSystemNotification(report.getCreateBy(), subject, message.toString());
    }

    public CustomPage<ReportDTO> queryReports(ReportPageQuery query) {
        Page<ReportPO> page = new Page<>(query.getPageNo(), query.getPageSize());
        LambdaQueryWrapper<ReportPO> wrapper = new LambdaQueryWrapper<>();

        if (query.getTargetType() != null) {
            wrapper.eq(ReportPO::getTargetType, query.getTargetType().getValue());
        }
        if (query.getForumId() != null && query.getForumId() > 0) {
            wrapper.eq(ReportPO::getForumId, query.getForumId());
        }
        if (query.getHandleStatus() != null) {
            wrapper.eq(ReportPO::getHandleStatus, query.getHandleStatus().getValue());
        }
        if (query.getReporterId() != null) {
            wrapper.eq(ReportPO::getCreateBy, query.getReporterId());
        }
        if (query.getHandlerId() != null) {
            wrapper.eq(ReportPO::getUpdateBy, query.getHandlerId());
        }
        wrapper.orderByDesc(ReportPO::getCreateTime);

        reportMapper.selectPage(page, wrapper);

        return DataBaseUtils.createCustomPage(page, po -> toDTO(poToEntity(po)));
    }

    public long getPendingReportCount(Integer forumId) {
        LambdaQueryWrapper<ReportPO> wrapper = new LambdaQueryWrapper<>();
        if (forumId != null && forumId > 0) {
            wrapper.eq(ReportPO::getForumId, forumId);
        }
        wrapper.eq(ReportPO::getHandleStatus, ReportHandleStatusEnum.PENDING.getValue());
        return reportMapper.selectCount(wrapper);
    }

    private ReportDTO toDTO(ReportE report) {
        ReportDTO dto = new ReportDTO();
        BeanUtils.copyProperties(report, dto);
        if (StringUtils.isEmpty(report.getReportedContent())) {
            dto.setReportedContent("无文字内容");
        }
        UserE reporter = userApplication.getByIdFromCache(report.getCreateBy());
        dto.setReporterName(reporter.getUserName());
        UserE handler = userApplication.getByIdFromCache(report.getUpdateBy());
        dto.setHandlerName(handler.getUserName());
        setRedirectUrl(dto);
        return dto;
    }

    private void setRedirectUrl(ReportDTO dto) {
        switch (dto.getTargetType()) {
            case THREAD:
                dto.setRedirectUrl("/t/" + dto.getTargetId());
                break;
            case COMMENT:
                dto.setRedirectUrl("/c/" + dto.getTargetId());
                break;
            case REPLY:
                CommentReplyE commentReply = commentGateway.getCommentReply(dto.getTargetId());
                if (commentReply != null) {
                    dto.setRedirectUrl("/c/" + commentReply.getCommentId() + "?replyId=" + commentReply.getReplyId());
                }
                break;
        }
    }

    /**
     * 实体转PO
     */
    private ReportPO entityToPo(ReportE entity) {
        if (entity == null) {
            return null;
        }
        ReportPO po = new ReportPO();
        po.setReportId(entity.getReportId());
        po.setTargetId(entity.getTargetId());
        if (entity.getTargetType() != null) {
            po.setTargetType(entity.getTargetType().getValue());
        }
        po.setForumId(entity.getForumId());
        if (entity.getReportType() != null) {
            po.setReportType(entity.getReportType().getValue());
        }
        po.setReportReason(entity.getReportReason());
        po.setReportedContent(entity.getReportedContent());
        if (entity.getHandleStatus() != null) {
            po.setHandleStatus(entity.getHandleStatus().getValue());
        }
        po.setHandleReason(entity.getHandleReason());
        po.setUserIp(entity.getUserIp());
        po.setUserAgent(entity.getUserAgent());
        po.setCreateBy(entity.getCreateBy());
        po.setCreateTime(entity.getCreateTime());
        po.setUpdateBy(entity.getUpdateBy());
        po.setUpdateTime(entity.getUpdateTime());
        return po;
    }

    /**
     * PO转实体
     */
    private ReportE poToEntity(ReportPO po) {
        if (po == null) {
            return null;
        }
        ReportE entity = new ReportE();
        entity.setReportId(po.getReportId());
        entity.setTargetId(po.getTargetId());
        entity.setTargetType(ReportTargetTypeEnum.of(po.getTargetType()));
        entity.setForumId(po.getForumId());
        entity.setReportType(ReportTypeEnum.of(po.getReportType()));
        entity.setReportReason(po.getReportReason());
        entity.setReportedContent(po.getReportedContent());
        entity.setHandleStatus(ReportHandleStatusEnum.of(po.getHandleStatus()));
        entity.setHandleReason(po.getHandleReason());
        entity.setUserIp(po.getUserIp());
        entity.setUserAgent(po.getUserAgent());
        entity.setCreateBy(po.getCreateBy());
        entity.setCreateTime(po.getCreateTime());
        entity.setUpdateBy(po.getUpdateBy());
        entity.setUpdateTime(po.getUpdateTime());
        return entity;
    }
} 