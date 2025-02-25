package com.leyuz.bbs.ban.mybatis;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyuz.bbs.ban.BanDO;
import com.leyuz.common.mybatis.PageQuery;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class BanServiceImpl extends ServiceImpl<BanMapper, BanDO> implements IBanService {

    @Override
    public Page<BanDO> queryBans(Integer forumId, PageQuery pageQuery) {
        QueryWrapper<BanDO> queryWrapper = pageQuery.toQueryWrapper();
        if (forumId != null && forumId > 0) {
            queryWrapper.eq("forum_id", forumId);
        }
        Map<String, Object> params = pageQuery.getParams();
        Long userId = MapUtil.getLong(params, "userId");
        if (userId != null) {
            queryWrapper.eq("user_id", userId);
        }
        // 分页参数
        return page(pageQuery.toPage(), queryWrapper);
    }
}
