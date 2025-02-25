package com.leyuz.bbs.ban.mybatis;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.leyuz.bbs.ban.BanDO;
import com.leyuz.common.mybatis.PageQuery;

/**
 * 禁言记录表
 */
public interface IBanService extends IService<BanDO> {

    Page<BanDO> queryBans(Integer forumId, PageQuery pageQuery);
}
