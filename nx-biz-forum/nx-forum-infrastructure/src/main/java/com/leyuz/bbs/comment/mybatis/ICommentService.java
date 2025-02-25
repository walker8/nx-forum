package com.leyuz.bbs.comment.mybatis;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.leyuz.bbs.comment.CommentPO;
import com.leyuz.common.mybatis.PageQuery;

/**
 * <p>
 * 评论 服务类
 * </p>
 *
 * @author walker
 * @since 2024-07-28
 */
public interface ICommentService extends IService<CommentPO> {

    Page<CommentPO> queryComments(Integer forumId, PageQuery pageQuery);
}
