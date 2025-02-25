package com.leyuz.bbs.like.mybatis;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyuz.bbs.like.LikePO;
import com.leyuz.common.utils.HeaderUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author walker
 * @since 2024-09-18
 */
@Service
public class LikeServiceImpl extends ServiceImpl<LikeMapper, LikePO> implements ILikeService {
    @Override
    public LikePO findByUserAndTarget(Long userId, Integer targetType, Long targetId) {
        return getOne(new LambdaQueryWrapper<LikePO>()
                .eq(LikePO::getCreateBy, userId)
                .eq(LikePO::getTargetType, targetType)
                .eq(LikePO::getTargetId, targetId));
    }

    @Override
    public boolean save(LikePO po) {
        po.setCreateTime(LocalDateTime.now());
        po.setCreateBy(HeaderUtils.getUserId());
        return saveOrUpdate(po);
    }

    @Override
    public List<LikePO> getUserLikes(Long userId, Long threadId) {
        LambdaQueryWrapper<LikePO> queryWrapper = new LambdaQueryWrapper<LikePO>()
                .eq(LikePO::getCreateBy, userId)
                .eq(LikePO::getThreadId, threadId);
        return list(queryWrapper);
    }
}
