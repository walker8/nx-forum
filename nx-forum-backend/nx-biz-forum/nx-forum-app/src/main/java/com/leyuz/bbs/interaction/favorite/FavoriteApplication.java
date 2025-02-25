package com.leyuz.bbs.interaction.favorite;

import com.alibaba.cola.exception.BizException;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyuz.bbs.content.thread.gateway.ThreadGateway;
import com.leyuz.bbs.interaction.favorite.dto.FavoriteCmd;
import com.leyuz.bbs.interaction.favorite.FavoriteMapper;
import com.leyuz.bbs.content.thread.ThreadConvert;
import com.leyuz.bbs.content.thread.ThreadPO;
import com.leyuz.bbs.content.thread.dto.ThreadVO;
import com.leyuz.bbs.content.thread.ThreadMapper;
import com.leyuz.common.mybatis.CustomPage;
import com.leyuz.common.mybatis.DataBaseUtils;
import com.leyuz.common.utils.HeaderUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteApplication {

    private final FavoriteMapper favoriteMapper;
    private final ThreadGateway threadGateway;
    private final ThreadMapper threadMapper;
    private final ThreadConvert threadConvert;

    @Transactional
    public Integer toggleFavorite(FavoriteCmd cmd) {
        Long userId = HeaderUtils.getUserId();
        if (cmd.getIsFavorite()) {
            // 添加收藏
            FavoritePO favorite = FavoritePO.builder()
                    .threadId(cmd.getThreadId())
                    .build();
            favorite.setCreateBy(userId);
            favorite.setCreateTime(LocalDateTime.now());
            try {
                favoriteMapper.insert(favorite);
            } catch (Exception e) {
                throw new BizException("请勿重复收藏");
            }
            threadGateway.incrementCollectionCount(cmd.getThreadId(), 1);
            return 1;
        } else {
            // 移除收藏
            boolean removed = favoriteMapper.removeFavorite(cmd.getThreadId(), userId);
            if (!removed) {
                throw new BizException("收藏记录不存在");
            }
            threadGateway.incrementCollectionCount(cmd.getThreadId(), -1);
            return 0;
        }
    }

    public CustomPage<ThreadVO> queryThreadsByUserFavorites(Long userId, int pageNo, int pageSize) {
        Page<ThreadPO> page = new Page<>(pageNo, pageSize);
        List<ThreadPO> threadPOList = threadMapper.queryThreadsByUserFavorites(page, userId);
        page.setRecords(threadPOList);
        return DataBaseUtils.createCustomPage(page, po -> threadConvert.convertThreadPO2VO(po, false));
    }

    public Boolean isFavorite(Long threadId) {
        Long userId = HeaderUtils.getUserId();
        if (userId == null || userId == 0) {
            return false;
        }
        return favoriteMapper.isFavorite(threadId, userId);
    }
}