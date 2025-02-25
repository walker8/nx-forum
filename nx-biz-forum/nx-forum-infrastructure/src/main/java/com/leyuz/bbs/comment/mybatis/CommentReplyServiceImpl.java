package com.leyuz.bbs.comment.mybatis;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyuz.bbs.comment.CommentReplyPO;
import com.leyuz.common.mybatis.PageQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * <p>
 * 楼中楼评论 服务实现类
 * </p>
 *
 * @author walker
 * @since 2024-07-28
 */
@Service
public class CommentReplyServiceImpl extends ServiceImpl<CommentReplyMapper, CommentReplyPO> implements ICommentReplyService {

    @Override
    public Page<CommentReplyPO> queryCommentReplies(Integer forumId, PageQuery pageQuery) {
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
        // 分页参数
        return page(pageQuery.toPage(), queryWrapper);
    }
}
