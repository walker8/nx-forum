package com.leyuz.bbs.content.comment.convert;

import com.google.common.collect.Table;
import com.leyuz.bbs.content.comment.CommentE;
import com.leyuz.bbs.content.comment.CommentReplyE;
import com.leyuz.bbs.content.comment.dto.CommentReplyVO;
import com.leyuz.bbs.content.comment.dto.CommentVO;
import com.leyuz.bbs.interaction.like.LikeApplication;
import com.leyuz.bbs.interaction.like.dto.LikeTargetType;
import com.leyuz.common.mybatis.CustomPage;
import com.leyuz.common.utils.TimeUtils;
import com.leyuz.uc.user.UserApplication;
import com.leyuz.uc.user.UserE;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CommentConvert {
    private final UserApplication userApplication;
    private final LikeApplication likeApplication;

    public CommentVO toCommentVO(CommentE commentE) {
        CommentVO commentVO = new CommentVO();
        BeanUtils.copyProperties(commentE, commentVO);
        commentVO.setMessage(commentE.getMessageHtml());
        Long createBy = commentE.getCreateBy();
        UserE user = userApplication.getByIdFromCache(createBy);
        commentVO.setAuthorId(createBy);
        commentVO.setAuthorName(user.getUserName());
        commentVO.setAvatarUrl(user.getAvatar());
        commentVO.setCreateTime(TimeUtils.formatDateTime(commentE.getCreateTime()));
        if (!commentE.getCreateTime().isEqual(commentE.getUpdateTime())) {
            commentVO.setUpdateTime(TimeUtils.formatDateTime(commentE.getUpdateTime()));
        }
        Table<Long, Integer, Boolean> userLikes = likeApplication.getUserLikesByThreadId(commentVO.getThreadId());
        if (userLikes.contains(commentVO.getCommentId(), LikeTargetType.COMMENT.getValue())) {
            commentVO.setLiked(true);
        }
        return commentVO;
    }

    public CommentVO toCommentVOWithoutImages(CommentE commentE) {
        CommentVO commentVO = new CommentVO();
        BeanUtils.copyProperties(commentE, commentVO);
        commentVO.setMessage(commentE.getMessageHtmlWithoutImages());
        Long createBy = commentE.getCreateBy();
        UserE user = userApplication.getByIdFromCache(createBy);
        commentVO.setAuthorId(createBy);
        commentVO.setAuthorName(user.getUserName());
        commentVO.setAvatarUrl(user.getAvatar());
        commentVO.setCreateTime(TimeUtils.formatDateTime(commentE.getCreateTime()));
        if (!commentE.getCreateTime().isEqual(commentE.getUpdateTime())) {
            commentVO.setUpdateTime(TimeUtils.formatDateTime(commentE.getUpdateTime()));
        }
        return commentVO;
    }

    public CommentReplyVO toCommentReplyVO(CommentReplyE commentReplyE) {
        CommentReplyVO commentReplyVO = new CommentReplyVO();
        BeanUtils.copyProperties(commentReplyE, commentReplyVO);
        commentReplyVO.setMessage(commentReplyE.getMessageHtml());
        Long createBy = commentReplyE.getCreateBy();
        UserE user = userApplication.getByIdFromCache(createBy);
        commentReplyVO.setAuthorId(createBy);
        commentReplyVO.setAuthorName(user.getUserName());
        commentReplyVO.setAvatarUrl(user.getAvatar());
        commentReplyVO.setCreateTime(TimeUtils.formatDateTime(commentReplyE.getCreateTime()));
        if (commentReplyE.getReplyUserId() != null && commentReplyE.getReplyUserId() > 0) {
            UserE replyUser = userApplication.getByIdFromCache(commentReplyE.getReplyUserId());
            commentReplyVO.setReplyAuthorId(createBy);
            commentReplyVO.setReplyAuthorName(replyUser.getUserName());
            commentReplyVO.setReplyAvatarUrl(replyUser.getAvatar());
        }
        Table<Long, Integer, Boolean> userLikes = likeApplication.getUserLikesByThreadId(commentReplyVO.getThreadId());
        if (userLikes.contains(commentReplyVO.getReplyId(), LikeTargetType.REPLY.getValue())) {
            commentReplyVO.setLiked(true);
        }
        return commentReplyVO;
    }

    public List<CommentReplyVO> toCommentReplyVOList(CustomPage<CommentReplyE> commentReplyCustomPage) {
        List<CommentReplyE> commentReplyList = commentReplyCustomPage.getRecords();
        // 将查询结果转换为 CommentReplyVO 列表
        return commentReplyList.stream()
                .map(this::toCommentReplyVO)
                .toList();
    }

    public CustomPage<CommentReplyVO> toCommentReplyVOCustomPage(CustomPage<CommentReplyE> commentReplyCustomPage) {
        return CustomPage.<CommentReplyVO>builder()
                .current(commentReplyCustomPage.getCurrent())
                .hasNext(commentReplyCustomPage.isHasNext())
                .records(toCommentReplyVOList(commentReplyCustomPage))
                .size(commentReplyCustomPage.getSize())
                .total(commentReplyCustomPage.getTotal())
                .build();
    }
}
