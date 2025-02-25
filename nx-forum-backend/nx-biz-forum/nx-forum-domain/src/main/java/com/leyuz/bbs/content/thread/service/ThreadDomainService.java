package com.leyuz.bbs.content.thread.service;

import com.leyuz.bbs.common.constant.OperationConstant;
import com.leyuz.bbs.common.dataobject.AuditStatusV;
import com.leyuz.bbs.content.thread.ThreadE;
import com.leyuz.bbs.content.thread.ThreadPropertyE;
import com.leyuz.bbs.content.thread.dataobject.ThreadPropertyAttributeTopV;
import com.leyuz.bbs.content.thread.dataobject.ThreadPropertyTypeV;
import com.leyuz.bbs.content.thread.dataobject.ThreadPropertyV;
import com.leyuz.bbs.content.thread.event.*;
import com.leyuz.bbs.content.thread.event.dto.ThreadTransferredEventData;
import com.leyuz.bbs.content.thread.gateway.ThreadGateway;
import com.leyuz.common.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class ThreadDomainService {
    private final ApplicationEventPublisher eventPublisher;
    private final ThreadGateway threadGateway;

    @Transactional(rollbackFor = Exception.class)
    public void save(ThreadE threadE) {
        threadE.create();
        // 保存到数据库
        threadGateway.save(threadE);
        // 发布新帖创建事件
        eventPublisher.publishEvent(new ThreadNewEvent(this, threadE));
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(ThreadE threadE) {
        threadE.update();
        // 更新到数据库
        threadGateway.update(threadE);
        // 发布更新事件
        eventPublisher.publishEvent(new ThreadUpdateEvent(this, threadE));
    }

    public void handleNewComment(Long threadId, Long createBy) {
        // 主题帖+1
        threadGateway.increaseComments(threadId, 1);
        if (createBy != null) {
            // 更新最后回复人和回复时间
            threadGateway.updateLastComment(threadId, createBy);
        }
    }

    public void deleteThreads(Integer forumId, List<Long> threadIds, String reason, boolean notice) {
        operateThreads(forumId, threadIds, threadE -> {
            if (AuditStatusV.PASSED.equals(threadE.getAuditStatus())) {
                // 已通过的主题才能删除
                boolean deleted = threadGateway.deleteThread(threadE.getThreadId());
                if (deleted) {
                    eventPublisher.publishEvent(new ThreadDeletedEvent(this, threadE, reason, notice));
                }
            }
        });
    }

    public void passThreads(Integer forumId, List<Long> threadIds, Boolean notice) {
        operateThreads(forumId, threadIds, threadE -> {
            if (AuditStatusV.AUDITING.equals(threadE.getAuditStatus())) {
                // 待审核的主题才能通过
                boolean passed = threadGateway.passThread(threadE.getThreadId());
                if (passed) {
                    eventPublisher.publishEvent(new ThreadPassedEvent(this, threadE, notice));
                }
            }
        });
    }

    public void rejectThreads(Integer forumId, List<Long> threadIds, String reason, boolean notice) {
        operateThreads(forumId, threadIds, threadE -> {
            if (AuditStatusV.AUDITING.equals(threadE.getAuditStatus())) {
                // 待审核的帖子才能被拒绝
                boolean rejected = threadGateway.rejectThread(threadE.getThreadId(), reason);
                if (rejected) {
                    eventPublisher.publishEvent(new ThreadRejectedEvent(this, threadE, reason, notice));
                }
            }
        });
    }

    public void topThreads(Integer forumId, List<Long> threadIds) {
        changeProperty(forumId, threadIds, threadE -> {
            ThreadPropertyV property = threadE.getProperty();
            property.setTop(1);
            return ThreadPropertyE.builder().propertyType(ThreadPropertyTypeV.TOP)
                    .threadId(threadE.getThreadId()).forumId(threadE.getForumId())
                    .propertyAttribute(ThreadPropertyAttributeTopV.CURRENT_FORUM)
                    .operation(OperationConstant.TOP).build();
        });
    }

    public void globalTopThreads(Integer forumId, List<Long> threadIds) {
        changeProperty(forumId, threadIds, threadE -> {
            ThreadPropertyV property = threadE.getProperty();
            property.setTop(2);
            return ThreadPropertyE.builder().propertyType(ThreadPropertyTypeV.TOP)
                    .threadId(threadE.getThreadId()).forumId(threadE.getForumId())
                    .propertyAttribute(ThreadPropertyAttributeTopV.GLOBAL)
                    .operation(OperationConstant.GLOBAL_TOP).build();
        });
    }

    public void cancelTopThreads(Integer forumId, List<Long> threadIds) {
        changeProperty(forumId, threadIds, threadE -> {
            ThreadPropertyV property = threadE.getProperty();
            if (property.isTop()) {
                // 已置顶的帖子才能被取消置顶
                property.setTop(0);
                return ThreadPropertyE.builder().propertyType(ThreadPropertyTypeV.TOP)
                        .threadId(threadE.getThreadId()).forumId(threadE.getForumId())
                        .operation(OperationConstant.CANCEL_TOP).build();
            }
            return null;
        });
    }

    public void digestThreads(Integer forumId, List<Long> threadIds) {
        changeProperty(forumId, threadIds, threadE -> {
            ThreadPropertyV property = threadE.getProperty();
            if (!property.isDigest()) {
                // 已通过未加精的帖子才能被加精
                property.setDigest(true);
                return ThreadPropertyE.builder().propertyType(ThreadPropertyTypeV.DIGEST)
                        .threadId(threadE.getThreadId()).forumId(threadE.getForumId())
                        .operation(OperationConstant.DIGEST).build();
            }
            return null;
        });
    }

    public void cancelDigestThreads(Integer forumId, List<Long> threadIds) {
        changeProperty(forumId, threadIds, threadE -> {
            ThreadPropertyV property = threadE.getProperty();
            if (property.isDigest()) {
                // 已加精的帖子才能被取消加精
                property.setDigest(false);
                return ThreadPropertyE.builder().propertyType(ThreadPropertyTypeV.DIGEST)
                        .threadId(threadE.getThreadId()).forumId(threadE.getForumId())
                        .operation(OperationConstant.CANCEL_DIGEST).build();
            }
            return null;
        });
    }

    public void recommendThreads(Integer forumId, List<Long> threadIds) {
        changeProperty(forumId, threadIds, threadE -> {
            ThreadPropertyV property = threadE.getProperty();
            if (!property.isRecommended()) {
                // 没有推荐的帖子才能被推荐
                property.setRecommended(true);
                return ThreadPropertyE.builder().propertyType(ThreadPropertyTypeV.RECOMMEND)
                        .threadId(threadE.getThreadId()).forumId(threadE.getForumId())
                        .operation(OperationConstant.RECOMMEND).build();
            }
            return null;
        });
    }

    public void cancelRecommendThreads(Integer forumId, List<Long> threadIds) {
        changeProperty(forumId, threadIds, threadE -> {
            ThreadPropertyV property = threadE.getProperty();
            if (property.isRecommended()) {
                // 已推荐的帖子才能被取消推荐
                property.setRecommended(false);
                return ThreadPropertyE.builder().propertyType(ThreadPropertyTypeV.RECOMMEND)
                        .threadId(threadE.getThreadId()).forumId(threadE.getForumId())
                        .operation(OperationConstant.CANCEL_RECOMMEND).build();
            }
            return null;
        });
    }

    public void openThreads(Integer forumId, List<Long> threadIds) {
        changeProperty(forumId, threadIds, threadE -> {
            ThreadPropertyV property = threadE.getProperty();
            if (property.isClosed()) {
                // 已关闭的帖子才能被打开
                property.setClosed(false);
                return ThreadPropertyE.builder().propertyType(ThreadPropertyTypeV.CLOSED)
                        .threadId(threadE.getThreadId()).forumId(threadE.getForumId())
                        .operation(OperationConstant.OPEN).build();
            }
            return null;
        });
    }

    public void closeThreads(Integer forumId, List<Long> threadIds) {
        changeProperty(forumId, threadIds, threadE -> {
            ThreadPropertyV property = threadE.getProperty();
            if (!property.isClosed()) {
                // 已通过未关闭的帖子才能被关闭
                property.setClosed(true);
                return ThreadPropertyE.builder().propertyType(ThreadPropertyTypeV.CLOSED)
                        .threadId(threadE.getThreadId()).forumId(threadE.getForumId())
                        .operation(OperationConstant.CLOSE).build();
            }
            return null;
        });
    }

    public void restoreThreads(Integer forumId, List<Long> threadIds, boolean notice) {
        if (CollectionUtils.isEmpty(threadIds)) {
            return;
        }
        if (forumId == null) {
            forumId = 0;
        }
        for (Long threadId : threadIds) {
            ThreadE threadE = threadGateway.getDeletedThread(threadId);
            if (threadE != null) {
                if (forumId <= 0 || threadE.getForumId().equals(forumId)) {
                    // 已删除的帖子才能被恢复
                    boolean restored = threadGateway.restoreThread(threadE.getThreadId());
                    if (restored) {
                        eventPublisher.publishEvent(new ThreadRestoredEvent(this, threadE, notice));
                    }
                }
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void transferThreads(Integer targetForumId, List<Long> threadIds, Boolean notice) {
        if (CollectionUtils.isEmpty(threadIds)) {
            return;
        }
        if (targetForumId == null) {
            throw new ValidationException("目标版块不能为空");
        }
        if (targetForumId <= 0) {
            throw new ValidationException("目标版块不存在");
        }
        for (Long threadId : threadIds) {
            ThreadE threadE = threadGateway.getThread(threadId);
            if (threadE != null) {
                if (!targetForumId.equals(threadE.getForumId())) {
                    boolean transferred = threadGateway.updateForumId(threadE.getThreadId(), targetForumId);
                    if (transferred) {
                        // 发布转移事件
                        ThreadTransferredEventData eventData = ThreadTransferredEventData.builder()
                                .subject(threadE.getSubject()).brief(threadE.getBrief()).userId(threadE.getCreateBy())
                                .threadId(threadE.getThreadId()).targetForumId(targetForumId).build();
                        eventPublisher.publishEvent(new ThreadTransferredEvent(this, eventData, notice));
                    }
                }
            }
        }
    }

    private void changeProperty(Integer forumId, List<Long> threadIds, Function<ThreadE, ThreadPropertyE> propertyFunction) {
        operateThreads(forumId, threadIds, threadE -> {
            ThreadPropertyV property = threadE.getProperty();
            if (AuditStatusV.PASSED.equals(threadE.getAuditStatus())) {
                ThreadPropertyE threadPropertyE = propertyFunction.apply(threadE);
                if (threadPropertyE != null) {
                    threadGateway.updateProperty(threadPropertyE, property);
                    eventPublisher.publishEvent(new ThreadChangePropertyEvent(this, threadPropertyE));
                }
            }
        });
    }

    private void operateThreads(Integer forumId, List<Long> threadIds, Consumer<ThreadE> consumer) {
        if (CollectionUtils.isEmpty(threadIds)) {
            return;
        }
        if (forumId == null) {
            forumId = 0;
        }
        for (Long threadId : threadIds) {
            ThreadE threadE = threadGateway.getThread(threadId);
            if (threadE != null) {
                if (forumId <= 0 || threadE.getForumId().equals(forumId)) {
                    consumer.accept(threadE);
                }
            }
        }
    }
}
