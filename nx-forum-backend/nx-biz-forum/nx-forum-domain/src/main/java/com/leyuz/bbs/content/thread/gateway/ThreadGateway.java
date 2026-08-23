package com.leyuz.bbs.content.thread.gateway;

import com.leyuz.bbs.common.dataobject.ThreadHistoryItemV;
import com.leyuz.bbs.content.thread.ThreadE;
import com.leyuz.bbs.content.thread.ThreadPropertyE;
import com.leyuz.bbs.content.thread.dataobject.ThreadPropertyV;

import java.time.LocalDateTime;
import java.util.List;

public interface ThreadGateway {
    void save(ThreadE threadE);

    ThreadE getThread(Long threadId);

    ThreadE getThreadFromCache(Long threadId);

    /**
     * 获取已删除的主题帖
     *
     * @param threadId
     * @return
     */
    ThreadE getDeletedThread(Long threadId);

    ThreadE getThreadDetail(Long threadId);

    String getThreadContent(Long threadId);

    void update(ThreadE threadE);

    boolean increaseComments(Long threadId, int num);

    boolean decreaseComments(Long threadId, int num);

    /**
     * 更新最后评论
     *
     * @param threadId
     * @param createBy
     */
    void updateLastComment(Long threadId, Long createBy);

    long getAuditingCount(Integer forumId);

    boolean deleteThread(Long threadId);

    boolean passThread(Long threadId);

    boolean rejectThread(Long threadId, String reason);

    /**
     * 仅更新审核原因（保持审核中状态）
     *
     * @param threadId 帖子 ID
     * @param reason   审核原因
     * @return 是否成功
     */
    boolean updateAuditReason(Long threadId, String reason);

    boolean restoreThread(Long threadId);

    /**
     * 更新主题帖属性
     *
     * @param threadPropertyE 主题帖的单个属性
     * @param property        完整的属性
     * @return
     */
    boolean updateProperty(ThreadPropertyE threadPropertyE, ThreadPropertyV property);

    boolean updateForumId(Long threadId, Integer newForumId);

    void incrementLikeCount(Long targetId, int delta);

    void incrementCollectionCount(Long threadId, int delta);

    /**
     * Count total threads (approved, not deleted)
     */
    Long countTotalThreads();

    /**
     * Count threads created between start and end date
     */
    Long countThreadsCreatedBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 统计指定时间段内创建的主题数量（按终端和平台）
     *
     * @param startDate     开始时间
     * @param endDate       结束时间
     * @param terminalType  终端类型（PC/MOBILE/APP/ALL）
     * @param platform      平台（Windows/Mac/Android/iPhone/ALL等）
     * @return 主题数量
     */
    Long countThreadsCreatedBetween(LocalDateTime startDate, LocalDateTime endDate,
                                    String terminalType, String platform);

    /**
     * 获取用户最近的主题帖（按发帖时间倒序，取 limit 条；包含已通过审核的帖子）
     *
     * @param userId 用户 ID
     * @param limit  最大条数；<=0 返回空列表
     * @return 历史主题帖条目列表
     */
    List<ThreadHistoryItemV> listRecentThreads(Long userId, int limit);
}
