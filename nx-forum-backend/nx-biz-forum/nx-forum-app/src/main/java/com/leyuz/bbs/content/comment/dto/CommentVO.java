package com.leyuz.bbs.content.comment.dto;

import lombok.Data;

import java.util.List;

@Data
public class CommentVO {
    private List<CommentReplyVO> replies;
    private Long commentId;
    /**
     * 楼中楼数
     */
    private Integer replyCount;

    /**
     * 正文中的图片，分隔符为#
     */
    private List<String> images;

    /**
     * 主题相关信息
     */
    private Long threadId;
    private String threadTitle;
    private Integer threadCommentCount;
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

    private String updateTime;

    private Long authorId;
    private String authorName;
    private String avatarUrl;
}
