package com.leyuz.bbs.interaction.like;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.leyuz.bbs.content.comment.CommentE;
import com.leyuz.bbs.content.comment.CommentReplyE;
import com.leyuz.bbs.content.comment.gateway.CommentGateway;
import com.leyuz.bbs.content.thread.ThreadE;
import com.leyuz.bbs.content.thread.gateway.ThreadGateway;
import com.leyuz.bbs.interaction.like.dto.LikeCmd;
import com.leyuz.bbs.interaction.like.LikeMapper;
import com.leyuz.bbs.content.thread.ThreadConvert;
import com.leyuz.bbs.content.thread.ThreadPO;
import com.leyuz.bbs.content.thread.dto.ThreadVO;
import com.leyuz.bbs.content.thread.ThreadMapper;
import com.leyuz.common.exception.ValidationException;
import com.leyuz.common.mybatis.CustomPage;
import com.leyuz.common.mybatis.DataBaseUtils;
import com.leyuz.common.utils.HeaderUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LikeApplication {

    private final LikeMapper likeMapper;
    private final ThreadGateway threadGateway;
    private final ThreadMapper threadMapper;
    private final CommentGateway commentGateway;
    private final ThreadConvert threadConvert;

    @Transactional
    public int toggleLike(LikeCmd cmd) {
        Long threadId = getThreadId(cmd.getTargetType(), cmd.getTargetId());

        LikePO existingLike = likeMapper.findByUserAndTarget(HeaderUtils.getUserId(), cmd.getTargetType(), cmd.getTargetId());

        if (cmd.getTargetType() < 0 || cmd.getTargetType() > 2) {
            throw new ValidationException("无效的目标类型");
        }

        if (existingLike == null) {
            // 新增点赞
            LikePO newLike = LikePO.builder()
                    .targetType(cmd.getTargetType())
                    .targetId(cmd.getTargetId())
                    .threadId(threadId)
                    .build();
            newLike.setCreateTime(java.time.LocalDateTime.now());
            newLike.setCreateBy(HeaderUtils.getUserId());
            likeMapper.insert(newLike);
            updateTargetLikeCount(cmd.getTargetType(), cmd.getTargetId(), 1);
            return 1;
        } else {
            // 取消点赞
            likeMapper.deleteById(existingLike.getId());
            updateTargetLikeCount(cmd.getTargetType(), cmd.getTargetId(), -1);
            return -1;
        }
    }

    private Long getThreadId(Integer targetType, Long targetId) {
        switch (targetType) {
            case 0:
                ThreadE threadE = threadGateway.getThreadFromCache(targetId);
                if (threadE != null) {
                    return threadE.getThreadId();
                } else {
                    throw new ValidationException("目标不存在或已删除");
                }
            case 1:
                CommentE comment = commentGateway.getComment(targetId);
                if (comment != null) {
                    return comment.getThreadId();
                } else {
                    throw new ValidationException("目标不存在或已删除");
                }
            case 2:
                CommentReplyE commentReply = commentGateway.getCommentReply(targetId);
                if (commentReply != null) {
                    return commentReply.getThreadId();
                } else {
                    throw new ValidationException("目标不存在或已删除");
                }
            default:
                throw new ValidationException("无效的目标类型");
        }
    }

    private void updateTargetLikeCount(Integer targetType, Long targetId, int delta) {
        switch (targetType) {
            case 0:
                threadGateway.incrementLikeCount(targetId, delta);
                break;
            case 1:
                commentGateway.incrementLikeCount(targetId, delta);
                break;
            case 2:
                commentGateway.incrementReplyLikeCount(targetId, delta);
                break;
        }
    }

    public CustomPage<ThreadVO> queryThreadsByUserLikes(Long userId, int pageNo, int pageSize) {
        Page<ThreadPO> page = new Page<>(pageNo, pageSize);
        List<ThreadPO> threadPOList = threadMapper.selectUserLikeList(page, userId);
        page.setRecords(threadPOList);
        return DataBaseUtils.createCustomPage(page, po -> threadConvert.convertThreadPO2VO(po, false));
    }

    public Table<Long, Integer, Boolean> getUserLikesByThreadId(Long threadId) {
        Long userId = HeaderUtils.getUserId();
        Table<Long, Integer, Boolean> table = HashBasedTable.create();
        if (userId == null || userId == 0) {
            return table;
        }
        List<LikePO> likePOList = likeMapper.getUserLikes(userId, threadId);
        for (LikePO likePO : likePOList) {
            table.put(likePO.getTargetId(), likePO.getTargetType(), true);
        }
        return table;
    }
}