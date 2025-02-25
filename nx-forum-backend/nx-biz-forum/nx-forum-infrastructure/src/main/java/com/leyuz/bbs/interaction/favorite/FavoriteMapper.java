package com.leyuz.bbs.interaction.favorite;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyuz.bbs.interaction.favorite.FavoritePO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 收藏 Mapper 接口
 */
@Mapper
public interface FavoriteMapper extends BaseMapper<FavoritePO> {

    /**
     * 判断是否已收藏
     *
     * @param threadId 帖子ID
     * @param userId   用户ID
     * @return 是否已收藏
     */
    default boolean isFavorite(Long threadId, Long userId) {
        QueryWrapper<FavoritePO> wrapper = new QueryWrapper<FavoritePO>()
                .eq("thread_id", threadId)
                .eq("create_by", userId);
        return selectCount(wrapper) > 0;
    }

    /**
     * 移除收藏
     *
     * @param threadId 帖子ID
     * @param userId   用户ID
     * @return 是否成功
     */
    default boolean removeFavorite(Long threadId, Long userId) {
        QueryWrapper<FavoritePO> wrapper = new QueryWrapper<FavoritePO>()
                .eq("thread_id", threadId)
                .eq("create_by", userId);
        return delete(wrapper) > 0;
    }
} 