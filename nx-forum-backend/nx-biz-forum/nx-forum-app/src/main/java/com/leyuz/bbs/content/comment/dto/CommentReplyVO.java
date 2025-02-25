package com.leyuz.bbs.content.comment.dto;

import lombok.Data;

@Data
public class CommentReplyVO {
    private Long commentId;
    private Long replyId;
    private Long threadId;
    /**
     * 点赞数
     */
    private Integer likes;
    private Boolean liked = false;
    /**
     * 内容
     */
    private String message;

    private String createTime;

    private Long authorId;
    private String authorName;
    private String avatarUrl;
    private Long replyAuthorId;
    private String replyAuthorName;
    private String replyAvatarUrl;
}
