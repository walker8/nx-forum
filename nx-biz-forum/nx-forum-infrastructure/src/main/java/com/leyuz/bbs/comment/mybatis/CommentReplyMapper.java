package com.leyuz.bbs.comment.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyuz.bbs.comment.CommentReplyPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 楼中楼评论 Mapper 接口
 * </p>
 *
 * @author walker
 * @since 2024-07-28
 */
@Mapper
public interface CommentReplyMapper extends BaseMapper<CommentReplyPO> {

}
