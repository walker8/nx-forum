package com.leyuz.bbs.thread.mybatis;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyuz.bbs.domain.thread.dataobject.ThreadPropertyAttributeTopV;
import com.leyuz.bbs.domain.thread.dataobject.ThreadPropertyTypeV;
import com.leyuz.bbs.thread.ThreadPO;
import com.leyuz.common.mybatis.PageQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author walker
 * @since 2024-03-31
 */
@Service
public class ThreadServiceImpl extends ServiceImpl<ThreadMapper, ThreadPO> implements IThreadService {
    @Autowired
    private ThreadMapper threadMapper;

    @Override
    public Page<ThreadPO> queryThreads(Integer forumId, PageQuery pageQuery) {
        Map<String, Object> params = pageQuery.getParams();
        String tableAlias;
        Integer propertyType = MapUtil.getInt(params, "propertyType");
        if (propertyType != null) {
            tableAlias = "t.";
        } else {
            tableAlias = "";
        }
        QueryWrapper<ThreadPO> queryWrapper = pageQuery.toQueryWrapper(tableAlias);
        if (forumId != null && forumId > 0) {
            queryWrapper.eq(tableAlias + "forum_id", forumId);
        }
        String keyword = MapUtil.getStr(params, "keyword");
        if (StringUtils.isNotBlank(keyword)) {
            queryWrapper.and(wrapper -> wrapper.like(tableAlias + "subject", keyword)
                    .or().like(tableAlias + "brief", keyword));
        }
        String ip = MapUtil.getStr(params, "ip");
        if (StringUtils.isNotBlank(ip)) {
            queryWrapper.eq(tableAlias + "user_ip", ip);
        }
        Long userId = MapUtil.getLong(params, "userId");
        if (userId != null) {
            queryWrapper.eq(tableAlias + "create_by", userId);
        }
        Integer auditStatus = MapUtil.getInt(params, "auditStatus");
        if (auditStatus != null) {
            queryWrapper.eq(tableAlias + "audit_status", auditStatus);
        }
        Integer days = MapUtil.getInt(params, "days");
        if (days != null && days > 0) {
            queryWrapper.ge(tableAlias + "create_time", System.currentTimeMillis() - days * 24 * 60 * 60 * 1000);
        }
        Map<String, Object> excludeThreadMap = MapUtil.getAny(params, "excludeThreadIds");
        if (!excludeThreadMap.isEmpty()) {
            List<Long> excludeThreadIds = (List<Long>) excludeThreadMap.get("excludeThreadIds");
            queryWrapper.notIn(tableAlias + "thread_id", excludeThreadIds);
        }
        if (propertyType == null) {
            // 分页参数
            return page(pageQuery.toPage(), queryWrapper);
        } else {
            queryWrapper.eq("p.property_type", propertyType);
            Page<ThreadPO> page = pageQuery.toPage();
            List<ThreadPO> threadPOList = threadMapper.selectPropertyList(page, queryWrapper);
            page.setRecords(threadPOList);
            return page;
        }
    }

    @Override
    public List<ThreadPO> queryTopThreads(Integer forumId) {
        Page<ThreadPO> page = new Page<>(1, 5);
        QueryWrapper<ThreadPO> queryWrapper = new QueryWrapper<>();
        if (forumId != null && forumId > 0) {
            queryWrapper.eq("p.forum_id", forumId);
            queryWrapper.eq("p.attribute", ThreadPropertyAttributeTopV.CURRENT_FORUM.getValue());
        } else {
            queryWrapper.eq("p.attribute", ThreadPropertyAttributeTopV.GLOBAL.getValue());
        }
        queryWrapper.eq("p.property_type", ThreadPropertyTypeV.TOP.getValue());
        return threadMapper.selectPropertyList(page, queryWrapper);
    }

    @Override
    public void updateViews(Long threadId, int count) {
        UpdateWrapper<ThreadPO> wrapper = new UpdateWrapper<ThreadPO>()
                .setSql("views = views + " + count)
                .eq("thread_id", threadId)
                .eq("is_deleted", false);
        update(wrapper);
    }

    @Override
    public Long getThreadCount(Integer forumId) {
        QueryWrapper<ThreadPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", false);
        if (forumId != null && forumId > 0) {
            queryWrapper.eq("forum_id", forumId);
        }
        return count(queryWrapper);
    }

    @Override
    public Page<ThreadPO> queryThreadsByUserIds(List<Long> userIds, Integer pageNo, Integer pageSize) {
        Page<ThreadPO> page = new Page<>(pageNo, pageSize);
        QueryWrapper<ThreadPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("create_by", userIds);
        queryWrapper.eq("is_deleted", false);
        queryWrapper.eq("audit_status", 0); // 只查询已通过审核的帖子
        queryWrapper.orderByDesc("create_time"); // 按创建时间倒序排序
        return page(page, queryWrapper);
    }
}
