package com.leyuz.bbs.favorite.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyuz.bbs.favorite.FavoritePO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FavoriteMapper extends BaseMapper<FavoritePO> {
} 