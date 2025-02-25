package com.leyuz.bbs.page.mybatis;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyuz.bbs.page.CustomPageContentPO;
import com.leyuz.bbs.page.CustomPagePO;
import com.leyuz.common.mybatis.PageQuery;
import com.leyuz.common.utils.BaseEntityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomPageServiceImpl implements CustomPageService {

    private final CustomPageMapper customPageMapper;
    private final CustomPageContentMapper customPageContentMapper;

    @Override
    public void save(CustomPagePO po) {
        BaseEntityUtils.setCreateBaseEntity(po);
        customPageMapper.insert(po);
    }

    @Override
    public void saveContent(Long pageId, String content) {
        QueryWrapper<CustomPageContentPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("page_id", pageId);
        queryWrapper.eq("is_deleted", false);
        CustomPageContentPO customPageContentPO = customPageContentMapper.selectOne(queryWrapper);
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
        customPageContentMapper.insertOrUpdate(customPageContentPO);
    }

    @Override
    public CustomPagePO getById(Long pageId) {
        CustomPagePO customPagePO = customPageMapper.selectById(pageId);
        if (customPagePO == null) {
            return null;
        }
        if (Boolean.TRUE.equals(customPagePO.getIsDeleted())) {
            return null;
        }
        return customPagePO;
    }

    @Override
    public boolean existsByPageCode(String pageCode) {
        QueryWrapper<CustomPagePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("page_code", pageCode);
        queryWrapper.eq("is_deleted", false);
        return customPageMapper.exists(queryWrapper);
    }

    @Override
    public boolean existsByPageId(Long pageId) {
        return customPageMapper.exists(new QueryWrapper<CustomPagePO>().eq("page_id", pageId));
    }

    @Override
    public CustomPageContentPO getContentByPageId(Long pageId) {
        return customPageContentMapper.selectOne(new QueryWrapper<CustomPageContentPO>().eq("page_id", pageId).eq("is_deleted", false));
    }

    @Override
    public CustomPagePO getByPageCode(String pageCode) {
        return customPageMapper.selectOne(new QueryWrapper<CustomPagePO>().eq("page_code", pageCode).eq("is_deleted", false));
    }

    @Override
    public Page<CustomPagePO> page(PageQuery query) {
        Page<CustomPagePO> page = new Page<>(query.getPageNo(), query.getPageSize());
        QueryWrapper<CustomPagePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", false);
        return customPageMapper.selectPage(page, queryWrapper);
    }

    @Override
    public void removeById(Long pageId) {
        CustomPagePO pagePO = getById(pageId);
        if (pagePO != null) {
            pagePO.setIsDeleted(true);
            customPageMapper.updateById(pagePO);
        }
    }

    @Override
    public void update(CustomPagePO pagePO) {
        BaseEntityUtils.setUpdateBaseEntity(pagePO);
        customPageMapper.updateById(pagePO);
    }
}