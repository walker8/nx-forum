package com.leyuz.bbs.interaction.like;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyuz.bbs.interaction.like.LikePO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * 点赞 Mapper 接口
 * </p>
 *
 * @author walker
 * @since 2024-09-18
 */
@Mapper
public interface LikeMapper extends BaseMapper<LikePO> {

    /**
     * 根据用户和目标查找点赞记录
     *
     * @param userId     用户ID
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @return 点赞记录
     */
    default LikePO findByUserAndTarget(Long userId, Integer targetType, Long targetId) {
        return selectOne(new LambdaQueryWrapper<LikePO>()
                .eq(LikePO::getCreateBy, userId)
                .eq(LikePO::getTargetType, targetType)
                .eq(LikePO::getTargetId, targetId));
    }

    /**
     * 获取用户在某个帖子下的所有点赞
     *
     * @param userId   用户ID
     * @param threadId 帖子ID
     * @return 点赞记录列表
     */
    default List<LikePO> getUserLikes(Long userId, Long threadId) {
        LambdaQueryWrapper<LikePO> queryWrapper = new LambdaQueryWrapper<LikePO>()
                .eq(LikePO::getCreateBy, userId)
                .eq(LikePO::getThreadId, threadId);
        return selectList(queryWrapper);
    }
}
