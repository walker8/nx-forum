package com.leyuz.bbs.content.thread;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.leyuz.bbs.common.dataobject.AuditStatusV;
import com.leyuz.bbs.common.dataobject.CommentOrderV;
import com.leyuz.bbs.common.dataobject.DocTypeV;
import com.leyuz.bbs.content.thread.ThreadE;
import com.leyuz.bbs.content.thread.ThreadPropertyE;
import com.leyuz.bbs.content.thread.dataobject.ThreadPropertyAttribute;
import com.leyuz.bbs.content.thread.dataobject.ThreadPropertyV;
import com.leyuz.bbs.content.thread.gateway.ThreadGateway;
import com.leyuz.bbs.content.thread.ThreadMapper;
import com.leyuz.bbs.content.thread.ThreadContentMapper;
import com.leyuz.bbs.content.thread.ThreadPropertyMapper;
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
    private final ThreadMapper threadMapper;
    private final ThreadContentMapper threadContentMapper;
    private final ThreadPropertyMapper threadPropertyMapper;

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
            threadMapper.insert(threadPO);
            threadContentPO.setThreadId(threadPO.getThreadId());
            threadContentPO.setContent(Optional.ofNullable(threadE.getContent()).orElse(""));
            BaseEntityUtils.setCreateBaseEntity(threadContentPO);
            threadContentMapper.insert(threadContentPO);
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
            threadMapper.updateById(threadPO);
            threadContentPO.setThreadId(threadPO.getThreadId());
            threadContentPO.setContent(Optional.ofNullable(threadE.getContent()).orElse(""));
            BaseEntityUtils.setUpdateBaseEntity(threadContentPO);
            threadContentMapper.update(threadContentPO, new UpdateWrapper<ThreadContentPO>().eq("thread_id", threadContentPO.getThreadId()));
        }
    }

    @Override
    public ThreadE getThread(Long threadId) {
        ThreadPO threadPO = threadMapper.selectById(threadId);
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
        ThreadPO threadPO = threadMapper.selectById(threadId);
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
            ThreadContentPO threadContentPO = threadContentMapper.selectOne(queryWrapper);
            threadE.setContent(threadContentPO.getContent());
        }
        return threadE;
    }

    @Override
    public String getThreadContent(Long threadId) {
        QueryWrapper<ThreadContentPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("thread_id", threadId);
        queryWrapper.eq("is_deleted", false);
        ThreadContentPO threadContentPO = threadContentMapper.selectOne(queryWrapper);
        if (threadContentPO != null) {
            return threadContentPO.getContent();
        } else {
            return "";
        }
    }

    @Override
    public boolean increaseComments(Long threadId, int num) {
        if (num <= 0) {
            return false;
        }
        LambdaUpdateWrapper<ThreadPO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ThreadPO::getThreadId, threadId);
        return threadMapper.update(null, wrapper.setSql("comments = comments + " + num)) > 0;
    }

    @Override
    public boolean decreaseComments(Long threadId, int num) {
        if (num <= 0) {
            return false;
        }
        LambdaUpdateWrapper<ThreadPO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ThreadPO::getThreadId, threadId);
        return threadMapper.update(null, wrapper.setSql("comments = comments - " + num)) > 0;
    }

    @Override
    public void updateLastComment(Long threadId, Long createBy) {
        LambdaUpdateWrapper<ThreadPO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ThreadPO::getThreadId, threadId)
                .set(ThreadPO::getLastCommentTime, LocalDateTime.now())
                .set(ThreadPO::getLastCommentUserId, createBy);
        threadMapper.update(null, wrapper);
    }

    @Override
    public long getAuditingCount(Integer forumId) {
        QueryWrapper<ThreadPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("audit_status", AuditStatusV.AUDITING.getValue());
        queryWrapper.eq("is_deleted", false);
        if (forumId != null && forumId > 0) {
            queryWrapper.eq("forum_id", forumId);
        }
        return threadMapper.selectCount(queryWrapper);
    }

    @Override
    public boolean deleteThread(Long threadId) {
        UpdateWrapper<ThreadPO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("thread_id", threadId).set("is_deleted", true);
        return threadMapper.update(null, updateWrapper) > 0;
    }

    @Override
    public boolean updateForumId(Long threadId, Integer newForumId) {
        UpdateWrapper<ThreadPO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("thread_id", threadId).set("forum_id", newForumId);
        return threadMapper.update(null, updateWrapper) > 0;
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
        threadMapper.update(null, wrapper);
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
        threadMapper.update(null, wrapper);
    }

    @Override
    public boolean updateProperty(ThreadPropertyE threadPropertyE, ThreadPropertyV property) {
        UpdateWrapper<ThreadPO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("thread_id", threadPropertyE.getThreadId())
                .set("property", JSON.toJSONString(property));
        if (property.isProperty(threadPropertyE.getPropertyType())) {
            // 先删除旧属性
            threadPropertyMapper.deleteByThreadId(threadPropertyE.getThreadId(), threadPropertyE.getPropertyType().getValue());
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
                threadPropertyMapper.insert(threadPropertyPO);
            }
        }
        return threadMapper.update(null, updateWrapper) > 0;
    }

    @Override
    public boolean passThread(Long threadId) {
        UpdateWrapper<ThreadPO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("thread_id", threadId).set("audit_status", AuditStatusV.PASSED.getValue());
        return threadMapper.update(null, updateWrapper) > 0;
    }

    @Override
    public boolean rejectThread(Long threadId, String reason) {
        UpdateWrapper<ThreadPO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("thread_id", threadId)
                .set("audit_status", AuditStatusV.REJECTED.getValue())
                .set("audit_reason", reason)
                .set("is_deleted", true);
        return threadMapper.update(null, updateWrapper) > 0;
    }

    @Override
    public boolean restoreThread(Long threadId) {
        UpdateWrapper<ThreadPO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("thread_id", threadId)
                .set("audit_status", AuditStatusV.PASSED.getValue())
                .set("is_deleted", false);
        return threadMapper.update(null, updateWrapper) > 0;
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
