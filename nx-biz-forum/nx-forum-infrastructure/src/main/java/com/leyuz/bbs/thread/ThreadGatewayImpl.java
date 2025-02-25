package com.leyuz.bbs.thread;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.leyuz.bbs.common.dataobject.AuditStatusV;
import com.leyuz.bbs.common.dataobject.CommentOrderV;
import com.leyuz.bbs.common.dataobject.DocTypeV;
import com.leyuz.bbs.domain.thread.ThreadE;
import com.leyuz.bbs.domain.thread.ThreadPropertyE;
import com.leyuz.bbs.domain.thread.dataobject.ThreadPropertyAttribute;
import com.leyuz.bbs.domain.thread.dataobject.ThreadPropertyV;
import com.leyuz.bbs.domain.thread.gateway.ThreadGateway;
import com.leyuz.bbs.thread.mybatis.IThreadContentService;
import com.leyuz.bbs.thread.mybatis.IThreadPropertyService;
import com.leyuz.bbs.thread.mybatis.IThreadService;
import com.leyuz.common.utils.BaseEntityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import static com.leyuz.bbs.common.constant.CommonConst.IMAGE_STRING_SEPARATOR;

@Repository
@RequiredArgsConstructor
public class ThreadGatewayImpl implements ThreadGateway {
    private final IThreadService threadService;
    private final IThreadContentService threadContentService;
    private final IThreadPropertyService threadPropertyService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void save(ThreadE threadE) {
        if (threadE != null) {
            ThreadPO threadPO = new ThreadPO();
            ThreadContentPO threadContentPO = new ThreadContentPO();
            BeanUtil.copyProperties(threadE, threadPO);
            threadPO.setProperty(JSON.toJSONString(threadE.getProperty()));
            BaseEntityUtils.setCreateBaseEntity(threadPO);
            threadPO.setImages(String.join(IMAGE_STRING_SEPARATOR, Optional.ofNullable(threadE.getImages()).orElse(new ArrayList<>())));
            threadService.save(threadPO);
            threadContentPO.setThreadId(threadPO.getThreadId());
            threadContentPO.setContent(Optional.ofNullable(threadE.getContent()).orElse(""));
            BaseEntityUtils.setCreateBaseEntity(threadContentPO);
            threadContentService.save(threadContentPO);
        }
    }

    @Override
    public void update(ThreadE threadE) {
        if (threadE != null && threadE.getThreadId() != null) {
            ThreadPO threadPO = new ThreadPO();
            ThreadContentPO threadContentPO = new ThreadContentPO();
            BeanUtil.copyProperties(threadE, threadPO);
            BaseEntityUtils.setUpdateBaseEntity(threadPO);
            threadPO.setImages(String.join(IMAGE_STRING_SEPARATOR, Optional.ofNullable(threadE.getImages()).orElse(new ArrayList<>())));
            threadPO.setProperty(null);
            threadService.updateById(threadPO);
            threadContentPO.setThreadId(threadPO.getThreadId());
            threadContentPO.setContent(Optional.ofNullable(threadE.getContent()).orElse(""));
            BaseEntityUtils.setUpdateBaseEntity(threadContentPO);
            threadContentService.update(threadContentPO, new UpdateWrapper<ThreadContentPO>().eq("thread_id", threadContentPO.getThreadId()));
        }
    }

    @Override
    public ThreadE getThread(Long threadId) {
        ThreadPO threadPO = threadService.getById(threadId);
        if (threadPO == null) {
            return null;
        }
        if (Objects.equals(threadPO.getIsDeleted(), false)) {
            return convert(threadPO);
        }
        return null;
    }

    @Override
    public ThreadE getThreadFromCache(Long threadId) {
        return getThread(threadId);
    }

    @Override
    public ThreadE getDeletedThread(Long threadId) {
        ThreadPO threadPO = threadService.getById(threadId);
        if (threadPO == null) {
            return null;
        }
        if (Objects.equals(threadPO.getIsDeleted(), true)) {
            return convert(threadPO);
        }
        return null;
    }

    @Override
    public ThreadE getThreadDetail(Long threadId) {
        ThreadE threadE = getThread(threadId);
        if (threadE != null) {
            QueryWrapper<ThreadContentPO> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("thread_id", threadId);
            queryWrapper.eq("is_deleted", false);
            ThreadContentPO threadContentPO = threadContentService.getOne(queryWrapper);
            threadE.setContent(threadContentPO.getContent());
        }
        return threadE;
    }

    @Override
    public boolean increaseComments(Long threadId, int num) {
        if (num <= 0) {
            return false;
        }
        LambdaUpdateWrapper<ThreadPO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ThreadPO::getThreadId, threadId);
        return threadService.update(null, wrapper.setSql("comments = comments + " + num));
    }

    @Override
    public boolean decreaseComments(Long threadId, int num) {
        if (num <= 0) {
            return false;
        }
        LambdaUpdateWrapper<ThreadPO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ThreadPO::getThreadId, threadId);
        return threadService.update(null, wrapper.setSql("comments = comments - " + num));
    }

    @Override
    public void updateLastComment(Long threadId, Long createBy) {
        LambdaUpdateWrapper<ThreadPO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ThreadPO::getThreadId, threadId)
                .set(ThreadPO::getLastCommentTime, LocalDateTime.now())
                .set(ThreadPO::getLastCommentUserId, createBy);
        threadService.update(null, wrapper);
    }

    @Override
    public long getAuditingCount(Integer forumId) {
        QueryWrapper<ThreadPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("audit_status", AuditStatusV.AUDITING.getValue());
        queryWrapper.eq("is_deleted", false);
        if (forumId != null && forumId > 0) {
            queryWrapper.eq("forum_id", forumId);
        }
        return threadService.count(queryWrapper);
    }

    @Override
    public boolean deleteThread(Long threadId) {
        UpdateWrapper<ThreadPO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("thread_id", threadId).set("is_deleted", true);
        return threadService.update(null, updateWrapper);
    }

    @Override
    public boolean updateForumId(Long threadId, Integer newForumId) {
        UpdateWrapper<ThreadPO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("thread_id", threadId).set("forum_id", newForumId);
        return threadService.update(null, updateWrapper);
    }

    @Override
    public void incrementLikeCount(Long targetId, int delta) {
        LambdaUpdateWrapper<ThreadPO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ThreadPO::getThreadId, targetId);
        if (delta > 0) {
            wrapper.setSql("likes = likes + " + delta);
        } else {
            wrapper.setSql("likes = likes - " + (-delta));
        }
        threadService.update(null, wrapper);
    }

    @Override
    public void incrementCollectionCount(Long threadId, int delta) {
        LambdaUpdateWrapper<ThreadPO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ThreadPO::getThreadId, threadId);
        if (delta > 0) {
            wrapper.setSql("collections = collections + " + delta);
        } else {
            wrapper.setSql("collections = collections - " + (-delta));
        }
        threadService.update(null, wrapper);
    }

    @Override
    public boolean updateProperty(ThreadPropertyE threadPropertyE, ThreadPropertyV property) {
        UpdateWrapper<ThreadPO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("thread_id", threadPropertyE.getThreadId())
                .set("property", JSON.toJSONString(property));
        if (property.isProperty(threadPropertyE.getPropertyType())) {
            // 先删除旧属性
            threadPropertyService.deleteByThreadId(threadPropertyE.getThreadId(), threadPropertyE.getPropertyType().getValue());
            // 再保存新属性
            if (property.getValue(threadPropertyE.getPropertyType())) {
                ThreadPropertyPO threadPropertyPO = new ThreadPropertyPO();
                threadPropertyPO.setThreadId(threadPropertyE.getThreadId());
                threadPropertyPO.setForumId(threadPropertyE.getForumId());
                threadPropertyPO.setPropertyType(threadPropertyE.getPropertyType().getValue());
                ThreadPropertyAttribute propertyAttribute = threadPropertyE.getPropertyAttribute();
                if (propertyAttribute != null) {
                    // 属性不一定有值
                    threadPropertyPO.setAttribute(propertyAttribute.getValue());
                }
                BaseEntityUtils.setCreateBaseEntity(threadPropertyPO);
                threadPropertyService.save(threadPropertyPO);
            }
        }
        return threadService.update(null, updateWrapper);
    }

    @Override
    public boolean passThread(Long threadId) {
        UpdateWrapper<ThreadPO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("thread_id", threadId).set("audit_status", AuditStatusV.PASSED.getValue());
        return threadService.update(null, updateWrapper);
    }

    @Override
    public boolean rejectThread(Long threadId, String reason) {
        UpdateWrapper<ThreadPO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("thread_id", threadId)
                .set("audit_status", AuditStatusV.REJECTED.getValue())
                .set("audit_reason", reason)
                .set("is_deleted", true);
        return threadService.update(null, updateWrapper);
    }

    @Override
    public boolean restoreThread(Long threadId) {
        UpdateWrapper<ThreadPO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("thread_id", threadId)
                .set("audit_status", AuditStatusV.PASSED.getValue())
                .set("is_deleted", false);
        return threadService.update(null, updateWrapper);
    }

    private ThreadE convert(ThreadPO threadPO) {
        if (threadPO == null) {
            return null;
        }
        return ThreadE.builder()
                .threadId(threadPO.getThreadId())
                .auditStatus(AuditStatusV.of(threadPO.getAuditStatus()))
                .brief(threadPO.getBrief())
                .categoryId(threadPO.getCategoryId())
                .property(JSON.parseObject(threadPO.getProperty(), ThreadPropertyV.class))
                .commentOrder(CommentOrderV.of(threadPO.getCommentOrder()))
                .comments(threadPO.getComments())
                .dislikes(threadPO.getDislikes())
                .likes(threadPO.getLikes())
                .userAgent(threadPO.getUserAgent())
                .userIp(threadPO.getUserIp())
                .createBy(threadPO.getCreateBy())
                .subject(threadPO.getSubject())
                .views(threadPO.getViews())
                .imageCount(threadPO.getImageCount())
                .images(Arrays.stream(Optional.ofNullable(threadPO.getImages()).orElse("").split(IMAGE_STRING_SEPARATOR)).toList())
                .lastCommentTime(threadPO.getLastCommentTime())
                .lastCommentUserId(threadPO.getLastCommentUserId())
                .collections(threadPO.getCollections())
                .docType(DocTypeV.of(threadPO.getDocType()))
                .forumId(threadPO.getForumId())
                .updateBy(threadPO.getUpdateBy())
                .createTime(threadPO.getCreateTime())
                .updateTime(threadPO.getUpdateTime())
                .build();
    }
}
