package com.leyuz.bbs.content.comment;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyuz.bbs.content.comment.CommentReplyPO;
import com.leyuz.common.mybatis.PageQuery;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

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
    
    default Page<CommentReplyPO> queryCommentReplies(Integer forumId, PageQuery pageQuery) {
        QueryWrapper<CommentReplyPO> queryWrapper = pageQuery.toQueryWrapper();
        if (forumId != null && forumId > 0) {
            queryWrapper.eq("forum_id", forumId);
        }
        Map<String, Object> params = pageQuery.getParams();
        String ip = MapUtil.getStr(params, "ip");
        if (StringUtils.isNotBlank(ip)) {
            queryWrapper.eq("user_ip", ip);
        }
        Long userId = MapUtil.getLong(params, "userId");
        if (userId != null) {
            queryWrapper.eq("create_by", userId);
        }
        Integer auditStatus = MapUtil.getInt(params, "auditStatus");
        if (auditStatus != null) {
            queryWrapper.eq("audit_status", auditStatus);
        }
        return selectPage(pageQuery.toPage(), queryWrapper);
    }
}
