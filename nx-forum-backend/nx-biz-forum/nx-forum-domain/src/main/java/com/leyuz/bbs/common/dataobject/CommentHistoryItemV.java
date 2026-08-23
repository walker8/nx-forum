package com.leyuz.bbs.common.dataobject;

import lombok.Value;

import java.time.LocalDateTime;

/**
 * AI 审核历史评论/楼中楼条目（内容 + 评论时间）
 *
 * @author Walker
 */
@Value
public class CommentHistoryItemV {
    /**
     * 评论内容（对应 bbs_comments.message 或 bbs_comment_replies.message）
     */
    private String content;
    /**
     * 评论时间
     */
    private LocalDateTime createTime;
}