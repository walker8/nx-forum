package com.leyuz.bbs.system.audit.context;

import com.leyuz.bbs.common.dataobject.CommentHistoryItemV;
import com.leyuz.bbs.common.dataobject.ThreadHistoryItemV;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 审核上下文（规则/AI 入参的聚合）
 *
 * @author Walker
 */
@Getter
@Builder
public class AuditContext {
    private PosterInfo poster;
    private PostInfo post;
    private ContentInfo content;
    /**
     * 历史主题帖（标题/概要/发帖时间），仅在审核主题帖时填充
     */
    private List<ThreadHistoryItemV> recentThreads;
    /**
     * 历史评论/楼中楼（内容/时间），仅在审核评论或楼中楼时填充
     */
    private List<CommentHistoryItemV> recentComments;
}
