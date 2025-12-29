package com.leyuz.bbs.content.comment;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyuz.bbs.content.comment.CommentMapper;
import com.leyuz.bbs.content.comment.CommentReplyMapper;
import com.leyuz.bbs.common.constant.CommonConst;
import com.leyuz.bbs.common.dataobject.AuditStatusV;
import com.leyuz.bbs.common.dataobject.CommentOrderV;
import com.leyuz.bbs.common.dataobject.DocTypeV;
import com.leyuz.bbs.content.comment.CommentE;
import com.leyuz.bbs.content.comment.CommentReplyE;
import com.leyuz.bbs.content.comment.gateway.CommentGateway;
import com.leyuz.common.mybatis.CustomPage;
import com.leyuz.common.mybatis.DataBaseUtils;
import com.leyuz.common.utils.BaseEntityUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.*;

import static com.leyuz.bbs.common.constant.CommonConst.IMAGE_STRING_SEPARATOR;

@Repository
@RequiredArgsConstructor
public class CommentGatewayImpl implements CommentGateway {
    private final CommentMapper commentMapper;
    private final CommentReplyMapper commentReplyMapper;


    @Override
    public void saveComment(CommentE commentE) {
        if (commentE != null) {
            CommentPO commentPO = new CommentPO();
            BeanUtil.copyProperties(commentE, commentPO);
            BaseEntityUtils.setCreateBaseEntity(commentPO);
            commentPO.setImages(String.join(IMAGE_STRING_SEPARATOR, Optional.ofNullable(commentE.getImages()).orElse(new ArrayList<>())));
            commentMapper.insert(commentPO);
            commentE.setCommentId(commentPO.getCommentId());
            commentE.setCreateBy(commentPO.getCreateBy());
        }
    }

    @Override
    public void saveCommentReply(CommentReplyE commentReplyE) {
        if (commentReplyE != null) {
            CommentReplyPO commentReplyPO = new CommentReplyPO();
            BeanUtil.copyProperties(commentReplyE, commentReplyPO);
            BaseEntityUtils.setCreateBaseEntity(commentReplyPO);
            commentReplyMapper.insert(commentReplyPO);
            commentReplyE.setCreateBy(commentReplyPO.getCreateBy());
            commentReplyE.setReplyId(commentReplyPO.getReplyId());
        }
    }

    @Override
    public CommentE getComment(Long commentId) {
        CommentPO commentPO = commentMapper.selectById(commentId);
        if (commentPO == null) {
            return null;
        }
        if (Objects.equals(commentPO.getIsDeleted(), false)) {
            return convert(commentPO);
        }
        return null;
    }

    @Override
    public CommentE getCommentFromCache(Long commentId) {
        return getComment(commentId);
    }

    @Override
    public CommentE getDeletedComment(Long commentId) {
        CommentPO commentPO = commentMapper.selectById(commentId);
        if (commentPO == null) {
            return null;
        }
        if (Objects.equals(commentPO.getIsDeleted(), true)) {
            return convert(commentPO);
        }
        return null;
    }

    @Override
    public CommentReplyE getCommentReply(Long replyId) {
        CommentReplyPO commentReplyPO = commentReplyMapper.selectById(replyId);
        if (commentReplyPO == null) {
            return null;
        }
        if (Objects.equals(commentReplyPO.getIsDeleted(), false)) {
            return convert(commentReplyPO);
        }
        return null;
    }

    @Override
    public CommentReplyE getDeletedCommentReply(Long replyId) {
        CommentReplyPO commentReplyPO = commentReplyMapper.selectById(replyId);
        if (commentReplyPO == null) {
            return null;
        }
        if (Objects.equals(commentReplyPO.getIsDeleted(), true)) {
            return convert(commentReplyPO);
        }
        return null;
    }

    @Override
    public boolean increaseReplies(Long commentId, int number) {
        if (number <= 0) {
            return false;
        }
        LambdaUpdateWrapper<CommentPO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(CommentPO::getCommentId, commentId);
        return commentMapper.update(null, wrapper.setSql("reply_count = reply_count + " + number)) > 0;
    }

    @Override
    public boolean decreaseReplies(Long commentId, int number) {
        if (number <= 0) {
            return false;
        }
        LambdaUpdateWrapper<CommentPO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(CommentPO::getCommentId, commentId);
        return commentMapper.update(null, wrapper.setSql("reply_count = reply_count - " + number)) > 0;
    }

    @Override
    public boolean deleteComment(Long commentId) {
        UpdateWrapper<CommentPO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("comment_id", commentId).set("is_deleted", true);
        return commentMapper.update(null, updateWrapper) > 0;
    }

    @Override
    public boolean deleteCommentReply(Long replyId) {
        UpdateWrapper<CommentReplyPO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("reply_id", replyId).set("is_deleted", true);
        return commentReplyMapper.update(null, updateWrapper) > 0;
    }

    @Override
    public boolean restoreComment(Long commentId) {
        UpdateWrapper<CommentPO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("comment_id", commentId).set("is_deleted", false);
        return commentMapper.update(null, updateWrapper) > 0;
    }

    @Override
    public boolean restoreCommentReply(Long replyId) {
        UpdateWrapper<CommentReplyPO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("reply_id", replyId).set("is_deleted", false);
        return commentReplyMapper.update(null, updateWrapper) > 0;
    }

    @Override
    public boolean passComment(Long commentId) {
        UpdateWrapper<CommentPO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("comment_id", commentId).set("is_deleted", false)
                .set("audit_status", AuditStatusV.PASSED.getValue());
        return commentMapper.update(null, updateWrapper) > 0;
    }

    @Override
    public boolean passCommentReply(Long replyId) {
        UpdateWrapper<CommentReplyPO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("reply_id", replyId).set("is_deleted", false)
                .set("audit_status", AuditStatusV.PASSED.getValue());
        return commentReplyMapper.update(null, updateWrapper) > 0;
    }

    @Override
    public boolean rejectComment(Long commentId, String reason) {
        UpdateWrapper<CommentPO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("comment_id", commentId).set("is_deleted", true)
                .set("audit_status", AuditStatusV.REJECTED.getValue())
                .set("audit_reason", reason);
        return commentMapper.update(null, updateWrapper) > 0;
    }

    @Override
    public boolean rejectCommentReply(Long replyId, String reason) {
        UpdateWrapper<CommentReplyPO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("reply_id", replyId).set("is_deleted", true)
                .set("audit_status", AuditStatusV.REJECTED.getValue())
                .set("audit_reason", reason);
        return commentReplyMapper.update(null, updateWrapper) > 0;
    }

    @Override
    public CustomPage<CommentE> queryComments(Long threadId, Long commentId, CommentOrderV orderV, int pageNo, int pageSize) {
        // 创建分页对象
        Page<CommentPO> page = new Page<>(pageNo, pageSize);
        // 执行分页查询
        QueryWrapper<CommentPO> wrapper = new QueryWrapper<>();
        wrapper.eq("is_deleted", 0);
        wrapper.eq("audit_status", AuditStatusV.PASSED.getValue());
        wrapper.eq("thread_id", threadId);

        // 如果指定了commentId，则优先查询该评论
        if (commentId != null) {
            String orderBy = "";
            if (CommentOrderV.TIME_DESC.equals(orderV)) {
                orderBy = "create_time DESC";
            } else if (CommentOrderV.TIME_ASC.equals(orderV)) {
                orderBy = "create_time ASC";
            } else if (CommentOrderV.HOT.equals(orderV)) {
                orderBy = "likes DESC, create_time ASC";
            }
            wrapper.last("ORDER BY CASE WHEN comment_id = " + commentId + " THEN 0 ELSE 1 END," + orderBy);
        } else {
            // 使用常规排序
            queryOrderBy(orderV, wrapper);
        }

        Page<CommentPO> commentPOPage = commentMapper.selectPage(page, wrapper);
        return DataBaseUtils.createCustomPage(commentPOPage, this::convert);
    }

    @Override
    public CustomPage<CommentE> queryCommentsByUserId(Long userId, int pageNo, int pageSize) {
        // 创建分页对象
        Page<CommentPO> page = new Page<>(pageNo, pageSize);
        // 执行分页查询
        QueryWrapper<CommentPO> wrapper = new QueryWrapper<>();
        wrapper.eq("is_deleted", 0);
        wrapper.eq("audit_status", AuditStatusV.PASSED.getValue());
        wrapper.eq("create_by", userId);
        wrapper.orderByDesc("create_time");
        Page<CommentPO> commentPOPage = commentMapper.selectPage(page, wrapper);
        return DataBaseUtils.createCustomPage(commentPOPage, this::convert);
    }

    @Override
    public void updateForumId(Long threadId, Integer targetForumId) {
        UpdateWrapper<CommentPO> commentPOUpdateWrapper = new UpdateWrapper<>();
        commentPOUpdateWrapper.eq("thread_id", threadId).set("forum_id", targetForumId);
        commentMapper.update(null, commentPOUpdateWrapper);
        UpdateWrapper<CommentReplyPO> commentReplyPOUpdateWrapper = new UpdateWrapper<>();
        commentReplyPOUpdateWrapper.eq("thread_id", threadId).set("forum_id", targetForumId);
        commentReplyMapper.update(null, commentReplyPOUpdateWrapper);
    }

    @Override
    public void incrementLikeCount(Long targetId, int delta) {
        UpdateWrapper<CommentPO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("comment_id", targetId).setSql("likes = likes + " + delta);
        commentMapper.update(null, updateWrapper);
    }

    @Override
    public void incrementReplyLikeCount(Long targetId, int delta) {
        UpdateWrapper<CommentReplyPO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("reply_id", targetId).setSql("likes = likes + " + delta);
        commentReplyMapper.update(null, updateWrapper);
    }

    @Override
    public CustomPage<CommentReplyE> queryCommentRelies(Long commentId, Long replyId, CommentOrderV orderV, int pageNo, int pageSize) {
        // 创建分页对象
        Page<CommentReplyPO> page = new Page<>(pageNo, pageSize, true);
        // 执行分页查询
        QueryWrapper<CommentReplyPO> wrapper = new QueryWrapper<>();
        wrapper.eq("is_deleted", 0);
        wrapper.eq("audit_status", AuditStatusV.PASSED.getValue());
        wrapper.eq("comment_id", commentId);

        // 如果指定了replyId，则优先查询该回复
        if (replyId != null) {
            String orderBy = "";
            if (CommentOrderV.TIME_DESC.equals(orderV)) {
                orderBy = "create_time DESC";
            } else if (CommentOrderV.TIME_ASC.equals(orderV)) {
                orderBy = "create_time ASC";
            } else if (CommentOrderV.HOT.equals(orderV)) {
                orderBy = "likes DESC, create_time ASC";
            }
            wrapper.last("ORDER BY CASE WHEN reply_id = " + replyId + " THEN 0 ELSE 1 END," + orderBy);
        } else {
            // 使用常规排序
            queryOrderBy(orderV, wrapper);
        }

        Page<CommentReplyPO> commentReplyPOPage = commentReplyMapper.selectPage(page, wrapper);
        CustomPage<CommentReplyE> customPage = new CustomPage<>();
        customPage.setCurrent(commentReplyPOPage.getCurrent());
        customPage.setSize(commentReplyPOPage.getSize());
        customPage.setTotal(commentReplyPOPage.getTotal());
        customPage.setRecords(commentReplyPOPage.getRecords().stream().map(this::convert).toList());
        customPage.setHasNext(commentReplyPOPage.hasNext());
        return customPage;
    }

    private static <T> void queryOrderBy(CommentOrderV orderV, QueryWrapper<T> wrapper) {
        if (CommentOrderV.TIME_DESC.equals(orderV)) {
            wrapper.orderByDesc("create_time");
        } else if (CommentOrderV.TIME_ASC.equals(orderV)) {
            wrapper.orderByAsc("create_time");
        } else if (CommentOrderV.HOT.equals(orderV)) {
            wrapper.orderByDesc("likes");
            wrapper.orderByAsc("create_time");
        }
    }

    @Override
    public long getCommentAuditingCount(Integer forumId) {
        QueryWrapper<CommentPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("audit_status", AuditStatusV.AUDITING.getValue());
        queryWrapper.eq("is_deleted", false);
        if (forumId != null && forumId > 0) {
            queryWrapper.eq("forum_id", forumId);
        }
        return commentMapper.selectCount(queryWrapper);
    }

    @Override
    public long getReplyAuditingCount(Integer forumId) {
        QueryWrapper<CommentReplyPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("audit_status", AuditStatusV.AUDITING.getValue());
        queryWrapper.eq("is_deleted", false);
        if (forumId != null && forumId > 0) {
            queryWrapper.eq("forum_id", forumId);
        }
        return commentReplyMapper.selectCount(queryWrapper);
    }

    private CommentE convert(CommentPO commentPO) {
        List<String> images = StringUtils.isEmpty(commentPO.getImages()) ? new ArrayList<>() :
                Arrays.stream(commentPO.getImages().split(CommonConst.IMAGE_STRING_SEPARATOR)).toList();
        return CommentE.builder()
                .threadId(commentPO.getThreadId())
                .commentId(commentPO.getCommentId())
                .auditStatus(AuditStatusV.of(commentPO.getAuditStatus()))
                .auditReason(commentPO.getAuditReason())
                .likes(commentPO.getLikes())
                .replyCount(commentPO.getReplyCount())
                .userAgent(commentPO.getUserAgent())
                .userIp(commentPO.getUserIp())
                .createBy(commentPO.getCreateBy())
                .message(commentPO.getMessage())
                .images(images)
                .docType(DocTypeV.of(commentPO.getDocType()))
                .forumId(commentPO.getForumId())
                .updateBy(commentPO.getUpdateBy())
                .createTime(commentPO.getCreateTime())
                .updateTime(commentPO.getUpdateTime())
                .build();
    }

    private CommentReplyE convert(CommentReplyPO commentReplyPO) {
        return CommentReplyE.builder()
                .replyId(commentReplyPO.getReplyId())
                .replyUserId(commentReplyPO.getReplyUserId())
                .threadId(commentReplyPO.getThreadId())
                .commentId(commentReplyPO.getCommentId())
                .auditStatus(AuditStatusV.of(commentReplyPO.getAuditStatus()))
                .auditReason(commentReplyPO.getAuditReason())
                .likes(commentReplyPO.getLikes())
                .userAgent(commentReplyPO.getUserAgent())
                .userIp(commentReplyPO.getUserIp())
                .createBy(commentReplyPO.getCreateBy())
                .message(commentReplyPO.getMessage())
                .forumId(commentReplyPO.getForumId())
                .updateBy(commentReplyPO.getUpdateBy())
                .createTime(commentReplyPO.getCreateTime())
                .updateTime(commentReplyPO.getUpdateTime())
                .build();
    }
}
