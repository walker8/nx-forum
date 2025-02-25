package com.leyuz.bbs.like.mybatis;

import com.baomidou.mybatisplus.extension.service.IService;
import com.leyuz.bbs.like.LikePO;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author walker
 * @since 2024-09-18
 */
public interface ILikeService extends IService<LikePO> {
    LikePO findByUserAndTarget(Long userId, Integer targetType, Long targetId);

    @Override
    boolean save(LikePO likePO);

    List<LikePO> getUserLikes(Long userId, Long threadId);
}
