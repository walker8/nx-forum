package com.leyuz.bbs.content.thread.strategy;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyuz.bbs.content.thread.ThreadMapper;
import com.leyuz.bbs.content.thread.ThreadPO;
import com.leyuz.bbs.content.thread.dataobject.ThreadPropertyTypeV;
import com.leyuz.bbs.content.thread.dataobject.ThreadPropertyV;
import com.leyuz.bbs.content.thread.dto.ThreadQuery;
import com.leyuz.common.exception.ValidationException;
import com.leyuz.common.mybatis.PageQuery;
import com.leyuz.uc.user.UserApplication;
import com.leyuz.uc.user.UserE;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 帖子查询策略抽象基类
 *
 * @author walker
 * @since 2025-03-01
 */
@RequiredArgsConstructor
public abstract class AbstractThreadQueryStrategy implements ThreadQueryStrategy {

    protected final ThreadMapper threadMapper;
    protected final UserApplication userApplication;

    private static final List<String> orderByColumns = Arrays.asList("last_comment_time", "create_time", "update_time", "views");

    /**
     * 获取帖子分页结果（包含置顶帖）
     *
     * @param forumId     论坛ID
     * @param threadQuery 查询参数
     * @return 查询结果
     */
    protected Page<ThreadPO> getThreadPOPage(Integer forumId, ThreadQuery threadQuery) {
        Page<ThreadPO> threadPOPage;
        List<ThreadPO> topThreads = threadMapper.queryTopThreads(forumId);
        threadQuery.setExcludeThreadIds(topThreads.stream().map(ThreadPO::getThreadId).toList());
        threadPOPage = queryThreads(forumId, threadQuery);
        if (threadQuery.getPageNo() == 1 && !topThreads.isEmpty()) {
            topThreads.forEach(m -> changeTopProperty(m, true));
            List<ThreadPO> allThreads = new ArrayList<>(topThreads);
            threadPOPage.getRecords().forEach(m -> changeTopProperty(m, false));
            allThreads.addAll(threadPOPage.getRecords());
            threadPOPage.setRecords(allThreads);
        } else {
            threadPOPage.getRecords().forEach(m -> changeTopProperty(m, false));
        }
        return threadPOPage;
    }

    protected Page<ThreadPO> getThreadPOPageWithoutTop(Integer forumId, ThreadQuery threadQuery) {
        Page<ThreadPO> threadPOPage;
        threadPOPage = queryThreads(forumId, threadQuery);
        threadPOPage.getRecords().forEach(m -> changeTopProperty(m, false));
        return threadPOPage;
    }

    /**
     * 修改帖子置顶属性
     */
    private void changeTopProperty(ThreadPO threadPO, boolean value) {
        String property = threadPO.getProperty();
        ThreadPropertyV threadPropertyV = JSON.parseObject(property, ThreadPropertyV.class);
        if (threadPropertyV == null) {
            threadPropertyV = new ThreadPropertyV();
            threadPropertyV.init();
        }
        threadPropertyV.setTop(value ? 1 : 0);
        threadPO.setProperty(JSON.toJSONString(threadPropertyV));
    }

    /**
     * 查询帖子列表
     */
    protected Page<ThreadPO> queryThreads(Integer forumId, ThreadQuery threadQuery) {
        String orderByColumn = threadQuery.getOrderBy();
        if (StringUtils.isNotBlank(orderByColumn) && !orderByColumns.contains(orderByColumn)) {
            throw new ValidationException("排序字段不正确");
        }
        PageQuery pageQuery = PageQuery.builder()
                .pageNo(threadQuery.getPageNo())
                .pageSize(threadQuery.getPageSize())
                .orderByColumn(orderByColumn)
                .isAsc(false)
                .build();
        Map<String, Object> params = pageQuery.getParams();
        params.put("keyword", threadQuery.getKeyword());
        params.put("ip", threadQuery.getIp());
        Boolean isDeleted = threadQuery.getDeleted();
        if (!Boolean.TRUE.equals(isDeleted)) {
            params.put("auditStatus", threadQuery.getAuditStatusV().getValue());
        }
        params.put("isDeleted", isDeleted);
        List<Long> excludeThreadIds = threadQuery.getExcludeThreadIds();
        if (!CollectionUtils.isEmpty(excludeThreadIds)) {
            params.put("excludeThreadIds", excludeThreadIds);
        }
        ThreadPropertyTypeV propertyTypeV = threadQuery.getPropertyTypeV();
        if (propertyTypeV != null) {
            params.put("propertyType", propertyTypeV.getValue());
        }
        if (StringUtils.isNotBlank(threadQuery.getAuthorName())) {
            UserE userE = userApplication.getByUserNameFromCache(threadQuery.getAuthorName());
            if (userE != null) {
                params.put("userId", userE.getUserId());
            } else {
                params.put("userId", 0);
            }
        }
        params.put("days", threadQuery.getDays());
        return threadMapper.queryThreads(forumId, pageQuery);
    }
} 