package com.leyuz.bbs.content.thread.gateway;

import com.leyuz.bbs.content.thread.ThreadE;
import com.leyuz.bbs.content.thread.ThreadPropertyE;
import com.leyuz.bbs.content.thread.dataobject.ThreadPropertyV;

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
}
