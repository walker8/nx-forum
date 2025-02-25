package com.leyuz.bbs.system.page;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyuz.bbs.system.page.CustomPageContentPO;
import com.leyuz.common.utils.BaseEntityUtils;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CustomPageContentMapper extends BaseMapper<CustomPageContentPO> {
    
    default void saveContent(Long pageId, String content) {
        QueryWrapper<CustomPageContentPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("page_id", pageId);
        queryWrapper.eq("is_deleted", false);
        CustomPageContentPO customPageContentPO = selectOne(queryWrapper);
        if (customPageContentPO == null) {
            customPageContentPO = new CustomPageContentPO();
            customPageContentPO.setPageId(pageId);
            customPageContentPO.setContent(content);
            customPageContentPO.setVersion(1);
            BaseEntityUtils.setCreateBaseEntity(customPageContentPO);
        } else {
            customPageContentPO.setPageId(pageId);
            customPageContentPO.setContent(content);
            customPageContentPO.setVersion(customPageContentPO.getVersion() + 1);
            BaseEntityUtils.setUpdateBaseEntity(customPageContentPO);
        }
        insertOrUpdate(customPageContentPO);
    }
    
    default CustomPageContentPO getContentByPageId(Long pageId) {
        return selectOne(new QueryWrapper<CustomPageContentPO>().eq("page_id", pageId).eq("is_deleted", false));
    }
}