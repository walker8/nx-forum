package com.leyuz.bbs.page.mybatis;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyuz.bbs.page.CustomPageContentPO;
import com.leyuz.bbs.page.CustomPagePO;
import com.leyuz.common.mybatis.PageQuery;

public interface CustomPageService {
    void save(CustomPagePO po);

    boolean existsByPageCode(String pageCode);

    void saveContent(Long pageId, String content);

    boolean existsByPageId(Long pageId);

    CustomPagePO getById(Long pageId);

    CustomPageContentPO getContentByPageId(Long pageId);

    CustomPagePO getByPageCode(String pageCode);

    Page<CustomPagePO> page(PageQuery query);

    void removeById(Long pageId);

    void update(CustomPagePO pagePO);
}