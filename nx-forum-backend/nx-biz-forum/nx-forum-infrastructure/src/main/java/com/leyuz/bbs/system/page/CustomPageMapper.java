package com.leyuz.bbs.system.page;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyuz.bbs.system.page.CustomPageContentPO;
import com.leyuz.bbs.system.page.CustomPagePO;
import com.leyuz.common.mybatis.PageQuery;
import com.leyuz.common.utils.BaseEntityUtils;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CustomPageMapper extends BaseMapper<CustomPagePO> {
    
    default void saveCustomPage(CustomPagePO po) {
        BaseEntityUtils.setCreateBaseEntity(po);
        insert(po);
    }
    
    default boolean existsByPageCode(String pageCode) {
        QueryWrapper<CustomPagePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("page_code", pageCode);
        queryWrapper.eq("is_deleted", false);
        return exists(queryWrapper);
    }
    
    default boolean existsByPageId(Long pageId) {
        return exists(new QueryWrapper<CustomPagePO>().eq("page_id", pageId));
    }
    
    default CustomPagePO getByPageId(Long pageId) {
        CustomPagePO customPagePO = selectById(pageId);
        if (customPagePO == null) {
            return null;
        }
        if (Boolean.TRUE.equals(customPagePO.getIsDeleted())) {
            return null;
        }
        return customPagePO;
    }
    
    default CustomPagePO getByPageCode(String pageCode) {
        return selectOne(new QueryWrapper<CustomPagePO>().eq("page_code", pageCode).eq("is_deleted", false));
    }
    
    default Page<CustomPagePO> queryPage(PageQuery query) {
        Page<CustomPagePO> page = new Page<>(query.getPageNo(), query.getPageSize());
        QueryWrapper<CustomPagePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", false);
        return selectPage(page, queryWrapper);
    }
    
    default void removeByPageId(Long pageId) {
        CustomPagePO pagePO = getByPageId(pageId);
        if (pagePO != null) {
            pagePO.setIsDeleted(true);
            updateById(pagePO);
        }
    }
    
    default void updatePage(CustomPagePO pagePO) {
        BaseEntityUtils.setUpdateBaseEntity(pagePO);
        updateById(pagePO);
    }
}