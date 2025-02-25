package com.leyuz.uc.user.mybatis;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyuz.common.mybatis.PageQuery;
import com.leyuz.uc.user.UserPO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author walker
 * @since 2024-01-06
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserPO> implements IUserService {

    @Override
    public Page<UserPO> queryUsers(PageQuery pageQuery) {
        QueryWrapper<UserPO> queryWrapper = pageQuery.toQueryWrapper();
        Map<String, Object> params = pageQuery.getParams();
        String loginIp = MapUtil.getStr(params, "loginIp");
        if (StringUtils.isNotBlank(loginIp)) {
            queryWrapper.eq("login_ip", loginIp);
        }
        Long userId = MapUtil.getLong(params, "userId");
        if (userId != null) {
            queryWrapper.eq("user_id", userId);
        }
        String userName = MapUtil.getStr(params, "userName");
        if (StringUtils.isNotBlank(userName)) {
            queryWrapper.like("user_name", userName);
        }
        // 分页参数
        return page(pageQuery.toPage(), queryWrapper);
    }

    @Override
    public List<UserPO> queryUsersByName(String name, int num) {
        QueryWrapper<UserPO> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(name)) {
            queryWrapper.like("user_name", name);
        }
        queryWrapper.last("limit " + num);
        queryWrapper.eq("is_deleted", false);
        return list(queryWrapper);
    }
}
