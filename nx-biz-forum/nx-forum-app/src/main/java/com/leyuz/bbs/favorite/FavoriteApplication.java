package com.leyuz.bbs.favorite;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyuz.bbs.domain.thread.gateway.ThreadGateway;
import com.leyuz.bbs.favorite.dto.FavoriteCmd;
import com.leyuz.bbs.favorite.mybatis.IFavoriteService;
import com.leyuz.bbs.thread.ThreadConvert;
import com.leyuz.bbs.thread.ThreadPO;
import com.leyuz.bbs.thread.dto.ThreadVO;
import com.leyuz.bbs.thread.mybatis.ThreadMapper;
import com.leyuz.common.mybatis.CustomPage;
import com.leyuz.common.mybatis.DataBaseUtils;
import com.leyuz.common.utils.HeaderUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteApplication {

    private final IFavoriteService favoriteService;
    private final ThreadGateway threadGateway;
    private final ThreadMapper threadMapper;
    private final ThreadConvert threadConvert;

    public Integer toggleFavorite(FavoriteCmd cmd) {
        if (cmd.getIsFavorite()) {
            favoriteService.addFavorite(cmd.getThreadId());
            threadGateway.incrementCollectionCount(cmd.getThreadId(), 1);
            return 1;
        } else {
            favoriteService.removeFavorite(cmd.getThreadId());
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
        if (HeaderUtils.getUserId() == 0) {
            return false;
        }
        return favoriteService.isFavorite(threadId);
    }
}