package com.leyuz.bbs.content.comment;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyuz.bbs.auth.ForumPermissionResolver;
import com.leyuz.bbs.common.constant.OperationConstant;
import com.leyuz.bbs.common.dataobject.AuditStageV;
import com.leyuz.bbs.common.dataobject.AuditStatusV;
import com.leyuz.bbs.common.dataobject.AuditTriggerSourceV;
import com.leyuz.bbs.common.dataobject.CommentOrderV;
import com.leyuz.bbs.common.dataobject.DocTypeV;
import com.leyuz.bbs.common.dataobject.OperatorTypeV;
import com.leyuz.bbs.common.utils.HtmlUtils;
import com.leyuz.bbs.content.comment.convert.CommentConvert;
import com.leyuz.bbs.content.comment.dto.*;
import com.leyuz.bbs.content.comment.gateway.CommentGateway;
import com.leyuz.bbs.content.comment.service.CommentDomainService;
import com.leyuz.bbs.content.thread.ThreadE;
import com.leyuz.bbs.content.thread.gateway.ThreadGateway;
import com.leyuz.bbs.forum.ForumApplication;
import com.leyuz.bbs.forum.ForumPO;
import com.leyuz.bbs.system.audit.AuditApplication;
import com.leyuz.bbs.system.audit.AuditLogApplication;
import com.leyuz.bbs.system.audit.domain.AuditLogE;
import com.leyuz.bbs.system.audit.dto.AuditContentType;
import com.leyuz.bbs.system.audit.dto.AuditDTO;
import com.leyuz.bbs.system.audit.dto.AuditResult;
import com.leyuz.common.dto.UserClientInfo;
import com.leyuz.common.exception.AuditException;
import com.leyuz.common.exception.ValidationException;
import com.leyuz.common.ip.AddressUtils;
import com.leyuz.common.mybatis.CustomPage;
import com.leyuz.common.mybatis.DataBaseUtils;
import com.leyuz.common.mybatis.PageQuery;
import com.leyuz.common.utils.HeaderUtils;
import com.leyuz.common.utils.UserAgentUtils;
import com.leyuz.uc.user.UserApplication;
import com.leyuz.uc.user.UserE;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentApplication {
    private final CommentDomainService commentDomainService;
    private final CommentGateway commentGateway;
    private final ThreadGateway threadGateway;
    private final CommentConvert commentConvert;
    private final UserApplication userApplication;
    private final CommentMapper commentMapper;
    private final CommentReplyMapper commentReplyMapper;
    private final ForumApplication forumApplication;
    private final AuditApplication auditApplication;
    private final AuditLogApplication auditLogApplication;
    private final ForumPermissionResolver forumPermissionResolver;


    public void createComment(Long threadId, CommentCmd commentCmd) {
        Integer forumId = checkThreadStatusAndGetForumId(threadId);
        forumPermissionResolver.checkPermission(forumId, "comment:new");
        // 初始化回复
        CommentE commentE = CommentE.builder()
                .images(commentCmd.getImages())
                .message(commentCmd.getMessage())
                .docType(DocTypeV.TEXT)
                .threadId(threadId)
                .forumId(forumId)
                .build();

        // 审核评论
        AuditDTO auditDTO = AuditDTO.builder()
                .userId(HeaderUtils.getUserId())
                .message(HtmlUtils.convertHtmlToText(commentCmd.getMessage()))
                .contentType(AuditContentType.COMMENT)
                .forumId(forumId)
                .isNew(true)
                .build();
        AuditResult result = auditApplication.check(auditDTO);
        commentE.setAuditResult(result.getStatus(), result.getReason());

        // 保存评论
        commentDomainService.saveComment(commentE);
        // 内容入库后回填同步阶段审核记录的 contentId（黑/白名单、敏感词、规则引擎）
        auditLogApplication.updateContentIdBySessionId(result.getSessionId(), commentE.getCommentId());
        if (result.isAiPending()) {
            auditApplication.scheduleAiReview(AuditContentType.COMMENT, commentE.getCommentId(), forumId, true);
        }
        if (AuditStatusV.AUDITING.equals(commentE.getAuditStatus())) {
            throw new AuditException("评论审核中，请耐心等待审核通过！");
        }
    }

    private Integer checkThreadStatusAndGetForumId(Long threadId) {
        ThreadE thread = threadGateway.getThread(threadId);
        if (thread == null) {
            throw new ValidationException("帖子不存在或已删除！");
        }
        if (AuditStatusV.AUDITING.equals(thread.getAuditStatus())) {
            throw new ValidationException("帖子正在审核中，无法评论！");
        }
        if (thread.getProperty().isClosed()) {
            throw new ValidationException("帖子已关闭，无法评论！");
        }
        return thread.getForumId();
    }

    public void createCommentReply(Long commentId, CommentReplyCmd commentReplyCmd) {
        CommentE commentE = commentGateway.getComment(commentId);
        if (commentE == null) {
            throw new ValidationException("评论不存在或已删除！");
        }
        Integer forumId = checkThreadStatusAndGetForumId(commentE.getThreadId());
        forumPermissionResolver.checkPermission(forumId, "comment:new");
        // 初始化评论回复
        CommentReplyE commentReplyE = CommentReplyE.builder()
                .message(commentReplyCmd.getMessage())
                .threadId(commentE.getThreadId())
                .forumId(commentE.getForumId())
                .commentId(commentId)
                .replyUserId(commentReplyCmd.getReplyUserId())
                .build();
        commentReplyE.create();

        // 审核回复
        AuditDTO auditDTO = AuditDTO.builder()
                .userId(HeaderUtils.getUserId())
                .message(HtmlUtils.convertHtmlToText(commentReplyCmd.getMessage()))
                .contentType(AuditContentType.REPLY)
                .forumId(forumId)
                .isNew(true)
                .build();
        AuditResult result = auditApplication.check(auditDTO);
        commentReplyE.setAuditResult(result.getStatus(), result.getReason());

        // 保存评论的回复
        commentDomainService.saveCommentReply(commentReplyE);
        // 内容入库后回填同步阶段审核记录的 contentId（黑/白名单、敏感词、规则引擎）
        auditLogApplication.updateContentIdBySessionId(result.getSessionId(), commentReplyE.getReplyId());
        if (result.isAiPending()) {
            auditApplication.scheduleAiReview(AuditContentType.REPLY, commentReplyE.getReplyId(), forumId, true);
        }
        if (AuditStatusV.AUDITING.equals(commentReplyE.getAuditStatus())) {
            throw new AuditException("回复审核中，请耐心等待审核通过！");
        }
    }

    public CustomPage<CommentVO> queryComments(Long threadId, Long commentId, int order, int pageNo, int pageSize) {
        CustomPage<CommentE> commentCustomPage = commentGateway.queryComments(threadId, commentId, CommentOrderV.of(order), pageNo, pageSize);
        CustomPage<CommentVO> commentVOCustomPage = new CustomPage<>();
        BeanUtils.copyProperties(commentCustomPage, commentVOCustomPage);
        // 将查询结果转换为 CommentVO 列表
        List<CommentVO> commentList = commentCustomPage.getRecords().stream()
                .map(commentConvert::toCommentVO)
                .toList();
        // 补充评论回复列表
        commentList.forEach(commentVO -> {
            if (commentVO.getReplyCount() > 0) {
                CustomPage<CommentReplyE> commentReplyCustomPage = commentGateway.queryCommentRelies(commentVO.getCommentId(), null, CommentOrderV.TIME_ASC, 1, 2);
                List<CommentReplyVO> commentReplyVOList = commentConvert.toCommentReplyVOList(commentReplyCustomPage);
                commentVO.setReplies(commentReplyVOList);
            }
        });
        commentVOCustomPage.setRecords(commentList);
        return commentVOCustomPage;
    }

    public CustomPage<CommentVO> queryCommentsByUserId(Long userId, int pageNo, int pageSize) {
        CustomPage<CommentE> commentCustomPage = commentGateway.queryCommentsByUserId(userId, pageNo, pageSize);
        CustomPage<CommentVO> commentVOCustomPage = new CustomPage<>();
        BeanUtils.copyProperties(commentCustomPage, commentVOCustomPage);
        // 将查询结果转换为 CommentVO 列表
        List<CommentVO> commentList = commentCustomPage.getRecords().stream()
                .map(commentConvert::toCommentVOWithoutImages)
                .toList();
        // 补充帖子信息
        commentList.forEach(commentVO -> {
            ThreadE thread = threadGateway.getThreadFromCache(commentVO.getThreadId());
            if (thread != null) {
                commentVO.setThreadCommentCount(thread.getComments());
                commentVO.setThreadTitle(thread.getTitle());
            } else {
                commentVO.setThreadCommentCount(0);
                commentVO.setThreadTitle("该帖子已删除");
            }
        });
        commentVOCustomPage.setRecords(commentList);
        return commentVOCustomPage;
    }

    public CustomPage<CommentReplyVO> queryCommentReplies(Long commentId, Long replyId, int order, int pageNo, int pageSize) {
        CustomPage<CommentReplyE> commentReplyCustomPage = commentGateway.queryCommentRelies(commentId, replyId, CommentOrderV.of(order), pageNo, pageSize);
        return commentConvert.toCommentReplyVOCustomPage(commentReplyCustomPage);
    }

    public CustomPage<AdminCommentVO> queryCommentsByAdmin(CommentQuery query) {
        PageQuery pageQuery = PageQuery.builder()
                .pageNo(query.getPageNo())
                .pageSize(query.getPageSize())
                .orderByColumn("create_time")
                .isAsc(false)
                .build();
        Map<String, Object> params = pageQuery.getParams();
        params.put("ip", query.getIp());
        if (StringUtils.isNotBlank(query.getAuthorName())) {
            UserE userE = userApplication.getByUserNameFromCache(query.getAuthorName());
            if (userE != null) {
                params.put("userId", userE.getUserId());
            } else {
                params.put("userId", 0);
            }
        }
        params.put("auditStatus", query.getAuditStatusV() != null ? query.getAuditStatusV().getValue() : null);
        params.put("isDeleted", query.getDeleted());
        Page<CommentPO> commentPOPage = commentMapper.queryComments(query.getForumId(), pageQuery);
        return DataBaseUtils.createCustomPage(commentPOPage, this::toAdminCommentVO);
    }

    public CustomPage<AdminCommentReplyVO> queryCommentRepliesByAdmin(CommentReplyQuery query) {
        PageQuery pageQuery = PageQuery.builder()
                .pageNo(query.getPageNo())
                .pageSize(query.getPageSize())
                .orderByColumn("create_time")
                .isAsc(false)
                .build();
        Map<String, Object> params = pageQuery.getParams();
        params.put("ip", query.getIp());
        if (StringUtils.isNotBlank(query.getAuthorName())) {
            UserE userE = userApplication.getByUserNameFromCache(query.getAuthorName());
            if (userE != null) {
                params.put("userId", userE.getUserId());
            } else {
                params.put("userId", 0);
            }
        }
        params.put("auditStatus", query.getAuditStatusV() != null ? query.getAuditStatusV().getValue() : null);
        params.put("isDeleted", query.getDeleted());
        Page<CommentReplyPO> commentReplyPOPage = commentReplyMapper.queryCommentReplies(query.getForumId(), pageQuery);
        return DataBaseUtils.createCustomPage(commentReplyPOPage, this::toAdminCommentReplyVO);
    }

    public CommentVO getCommentVOById(Long commentId, Long replyId) {
        CommentE commentE = commentGateway.getComment(commentId);
        if (commentE == null) {
            throw new ValidationException("评论不存在或已被删除");
        }
        CommentVO commentVO = commentConvert.toCommentVO(commentE);
        // 补充帖子信息
        ThreadE thread = threadGateway.getThreadFromCache(commentVO.getThreadId());
        if (thread != null) {
            commentVO.setThreadCommentCount(thread.getComments());
            commentVO.setThreadTitle(thread.getTitle());
        } else {
            commentVO.setThreadCommentCount(0);
            commentVO.setThreadTitle("该帖子已删除");
        }
        if (commentVO.getReplyCount() > 0) {
            commentVO.setReplies(queryCommentReplies(commentId, replyId, CommentOrderV.TIME_DESC.getValue(), 1, 20).getRecords());
        }
        return commentVO;
    }

    private AdminCommentVO toAdminCommentVO(CommentPO commentPO) {
        AdminCommentVO commentVO = BeanUtil.toBean(commentPO, AdminCommentVO.class);
        Long authorId = commentPO.getCreateBy();
        commentVO.setAuthorId(authorId);
        commentVO.setAuthorName(userApplication.getByIdFromCache(authorId).getUserName());
        ForumPO forumPO = forumApplication.getForumById(commentPO.getForumId());
        if (forumPO != null) {
            commentVO.setForumNickName(forumPO.getNickName());
        }
        commentVO.setLocation(AddressUtils.getCityByIP(commentPO.getUserIp()));
        UserClientInfo clientInfo = UserAgentUtils.getClientInfo(commentPO.getUserAgent());
        commentVO.setOs(clientInfo.getOs());
        commentVO.setBrowser(clientInfo.getBrowser());
        commentVO.setTerminalType(clientInfo.getTerminalType());
        commentVO.setPlatform(clientInfo.getPlatform());
        commentVO.setUserAgent(commentPO.getUserAgent());
        return commentVO;
    }

    private AdminCommentReplyVO toAdminCommentReplyVO(CommentReplyPO commentReplyPO) {
        AdminCommentReplyVO commentReplyVO = BeanUtil.toBean(commentReplyPO, AdminCommentReplyVO.class);
        Long authorId = commentReplyPO.getCreateBy();
        commentReplyVO.setAuthorId(authorId);
        commentReplyVO.setAuthorName(userApplication.getByIdFromCache(authorId).getUserName());
        ForumPO forumPO = forumApplication.getForumById(commentReplyPO.getForumId());
        if (forumPO != null) {
            commentReplyVO.setForumNickName(forumPO.getNickName());
        }
        commentReplyVO.setLocation(AddressUtils.getCityByIP(commentReplyPO.getUserIp()));
        UserClientInfo clientInfo = UserAgentUtils.getClientInfo(commentReplyPO.getUserAgent());
        commentReplyVO.setOs(clientInfo.getOs());
        commentReplyVO.setBrowser(clientInfo.getBrowser());
        commentReplyVO.setTerminalType(clientInfo.getTerminalType());
        commentReplyVO.setPlatform(clientInfo.getPlatform());
        commentReplyVO.setUserAgent(commentReplyPO.getUserAgent());
        return commentReplyVO;
    }

    public long getCommentAuditingCount(Integer forumId) {
        return commentGateway.getCommentAuditingCount(forumId);
    }

    public long getReplyAuditingCount(Integer forumId) {
        return commentGateway.getReplyAuditingCount(forumId);
    }

    public void operateCommentsByAdmin(Integer forumId, List<Long> commentIds, String operation, String reason, Boolean notice) {
        AuditStatusV targetStatus = null;
        AuditStatusV beforeStatus = null;
        switch (operation) {
            case OperationConstant.PASS:
                forumPermissionResolver.checkPermission(forumId, "admin:comment:pass");
                targetStatus = AuditStatusV.PASSED;
                recordCommentManualLog(commentIds, AuditContentType.COMMENT.ordinal() + 1, operation, beforeStatus, targetStatus, reason);
                commentDomainService.passComments(forumId, commentIds, notice);
                break;
            case OperationConstant.REJECT:
                forumPermissionResolver.checkPermission(forumId, "admin:comment:reject");
                targetStatus = AuditStatusV.REJECTED;
                recordCommentManualLog(commentIds, AuditContentType.COMMENT.ordinal() + 1, operation, beforeStatus, targetStatus, reason);
                commentDomainService.rejectComments(forumId, commentIds, reason, notice);
                break;
            case OperationConstant.RESTORE:
                forumPermissionResolver.checkPermission(forumId, "admin:comment:restore");
                targetStatus = AuditStatusV.PASSED;
                beforeStatus = AuditStatusV.REJECTED;
                recordCommentManualLog(commentIds, AuditContentType.COMMENT.ordinal() + 1, operation, beforeStatus, targetStatus, reason);
                commentDomainService.restoreComments(forumId, commentIds, notice);
                break;
            case OperationConstant.DELETE:
                forumPermissionResolver.checkPermission(forumId, "admin:comment:delete");
                targetStatus = AuditStatusV.REJECTED;
                recordCommentManualLog(commentIds, AuditContentType.COMMENT.ordinal() + 1, operation, beforeStatus, targetStatus, reason);
                commentDomainService.deleteComments(forumId, commentIds, reason, notice);
                break;
            default:
                throw new ValidationException("操作类型不正确");
        }
    }

    public void deleteComment(Long commentId, String reason, Boolean notice) {
        commentDomainService.deleteComments(0, Collections.singletonList(commentId), reason, notice);
    }

    public void operateCommentRepliesByAdmin(Integer forumId, List<Long> replyIds, String operation, String reason, Boolean notice) {
        AuditStatusV targetStatus = null;
        AuditStatusV beforeStatus = null;
        switch (operation) {
            case OperationConstant.PASS:
                forumPermissionResolver.checkPermission(forumId, "admin:comment:pass");
                targetStatus = AuditStatusV.PASSED;
                recordCommentManualLog(replyIds, AuditContentType.REPLY.ordinal() + 1, operation, beforeStatus, targetStatus, reason);
                commentDomainService.passCommentReplies(forumId, replyIds, notice);
                break;
            case OperationConstant.REJECT:
                forumPermissionResolver.checkPermission(forumId, "admin:comment:reject");
                targetStatus = AuditStatusV.REJECTED;
                recordCommentManualLog(replyIds, AuditContentType.REPLY.ordinal() + 1, operation, beforeStatus, targetStatus, reason);
                commentDomainService.rejectCommentReplies(forumId, replyIds, reason, notice);
                break;
            case OperationConstant.RESTORE:
                forumPermissionResolver.checkPermission(forumId, "admin:comment:restore");
                targetStatus = AuditStatusV.PASSED;
                beforeStatus = AuditStatusV.REJECTED;
                recordCommentManualLog(replyIds, AuditContentType.REPLY.ordinal() + 1, operation, beforeStatus, targetStatus, reason);
                commentDomainService.restoreCommentReplies(forumId, replyIds, notice);
                break;
            case OperationConstant.DELETE:
                forumPermissionResolver.checkPermission(forumId, "admin:comment:delete");
                targetStatus = AuditStatusV.REJECTED;
                recordCommentManualLog(replyIds, AuditContentType.REPLY.ordinal() + 1, operation, beforeStatus, targetStatus, reason);
                commentDomainService.deleteCommentReplies(forumId, replyIds, reason, notice);
                break;
            default:
                throw new ValidationException("操作类型不正确");
        }
    }

    public void deleteCommentReply(Long replyId, String reason, Boolean notice) {
        commentDomainService.deleteCommentReplies(0, Collections.singletonList(replyId), reason, notice);
    }

    /**
     * 记录管理员人工操作的审核日志（评论/楼中楼）
     */
    private void recordCommentManualLog(List<Long> ids, Integer contentType, String action,
                                        AuditStatusV before, AuditStatusV after, String reason) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        Long operatorId = HeaderUtils.getUserId();
        for (Long id : ids) {
            try {
                AuditStatusV statusBefore = before;
                Integer forumIdVal = null;
                Long userId = null;
                if (contentType == AuditContentType.COMMENT.ordinal() + 1) {
                    CommentE commentE = commentGateway.getComment(id);
                    if (commentE == null) {
                        continue;
                    }
                    if (statusBefore == null) {
                        statusBefore = commentE.getAuditStatus();
                    }
                    forumIdVal = commentE.getForumId();
                    userId = commentE.getCreateBy();
                } else if (contentType == AuditContentType.REPLY.ordinal() + 1) {
                    CommentReplyE replyE = commentGateway.getCommentReply(id);
                    if (replyE == null) {
                        continue;
                    }
                    if (statusBefore == null) {
                        statusBefore = replyE.getAuditStatus();
                    }
                    forumIdVal = replyE.getForumId();
                    userId = replyE.getCreateBy();
                }
                auditLogApplication.record(AuditLogE.builder()
                        .contentType(contentType)
                        .contentId(id)
                        .forumId(forumIdVal)
                        .userId(userId)
                        .userIp(HeaderUtils.getIp())
                        .triggerSource(AuditTriggerSourceV.MANUAL)
                        .auditStage(AuditStageV.MANUAL)
                        .auditStatusBefore(statusBefore)
                        .auditStatusAfter(after)
                        .hitDetail(com.alibaba.fastjson2.JSON.toJSONString(Map.of("action", action)))
                        .operatorId(operatorId)
                        .operatorType(OperatorTypeV.ADMIN)
                        .reason(reason)
                        .build());
            } catch (Exception e) {
                log.error("写入评论/楼中楼人工审核日志失败，id={}", id, e);
            }
        }
    }
}
