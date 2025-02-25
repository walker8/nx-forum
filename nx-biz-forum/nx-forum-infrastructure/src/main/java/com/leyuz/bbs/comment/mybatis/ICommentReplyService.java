package com.leyuz.bbs.comment.mybatis;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.leyuz.bbs.comment.CommentReplyPO;
import com.leyuz.common.mybatis.PageQuery;

/**
 * <p>
 * 楼中楼评论 服务类
 * </p>
 *
 * @author walker
 * @since 2024-07-28
 */
public interface ICommentReplyService extends IService<CommentReplyPO> {

    Page<CommentReplyPO> queryCommentReplies(Integer forumId, PageQuery pageQuery);
}
