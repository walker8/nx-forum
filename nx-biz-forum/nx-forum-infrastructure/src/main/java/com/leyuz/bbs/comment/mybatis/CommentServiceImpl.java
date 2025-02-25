package com.leyuz.bbs.comment.mybatis;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyuz.bbs.comment.CommentPO;
import com.leyuz.common.mybatis.PageQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * <p>
 * 评论 服务实现类
 * </p>
 *
 * @author walker
 * @since 2024-07-28
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, CommentPO> implements ICommentService {

    @Override
    public Page<CommentPO> queryComments(Integer forumId, PageQuery pageQuery) {
        QueryWrapper<CommentPO> queryWrapper = pageQuery.toQueryWrapper();
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
