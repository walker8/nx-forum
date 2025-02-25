package com.leyuz.bbs.favorite.mybatis;

import com.baomidou.mybatisplus.extension.service.IService;
import com.leyuz.bbs.favorite.FavoritePO;

public interface IFavoriteService extends IService<FavoritePO> {

    void addFavorite(Long threadId);

    void removeFavorite(Long threadId);

    boolean isFavorite(Long threadId);
}
