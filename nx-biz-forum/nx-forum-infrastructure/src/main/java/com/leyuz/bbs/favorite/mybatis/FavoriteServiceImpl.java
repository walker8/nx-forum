package com.leyuz.bbs.favorite.mybatis;

import com.alibaba.cola.exception.BizException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyuz.bbs.favorite.FavoritePO;
import com.leyuz.common.utils.HeaderUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, FavoritePO> implements IFavoriteService {

    @Override
    @Transactional
    public void addFavorite(Long threadId) {
        FavoritePO favorite = FavoritePO.builder()
                .threadId(threadId)
                .build();
        favorite.setCreateBy(HeaderUtils.getUserId());
        favorite.setCreateTime(LocalDateTime.now());
        try {
            save(favorite);
        } catch (Exception e) {
            throw new BizException("请勿重复收藏");
        }
    }

    @Override
    @Transactional
    public void removeFavorite(Long threadId) {
        boolean removed = remove(new QueryWrapper<FavoritePO>()
                .eq("thread_id", threadId)
                .eq("create_by", HeaderUtils.getUserId()));

        if (!removed) {
            throw new BizException("收藏记录不存在");
        }
    }

    @Override
    public boolean isFavorite(Long threadId) {
        QueryWrapper<FavoritePO> wrapper = new QueryWrapper<FavoritePO>()
                .eq("thread_id", threadId)
                .eq("create_by", HeaderUtils.getUserId());
        return count(wrapper) > 0;
    }
}