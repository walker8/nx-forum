package com.leyuz.bbs.thread.mybatis;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.leyuz.bbs.thread.ThreadPO;
import com.leyuz.common.mybatis.PageQuery;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author walker
 * @since 2024-03-31
 */
public interface IThreadService extends IService<ThreadPO> {
    Page<ThreadPO> queryThreads(Integer forumId, PageQuery pageQuery);

    List<ThreadPO> queryTopThreads(Integer forumId);

    void updateViews(Long threadId, int increment);

    Long getThreadCount(Integer forumId);
    
    /**
     * 根据用户ID列表查询帖子
     *
     * @param userIds 用户ID列表
     * @param pageNo 页码
     * @param pageSize 每页大小
     * @return 帖子分页结果
     */
    Page<ThreadPO> queryThreadsByUserIds(List<Long> userIds, Integer pageNo, Integer pageSize);
}
