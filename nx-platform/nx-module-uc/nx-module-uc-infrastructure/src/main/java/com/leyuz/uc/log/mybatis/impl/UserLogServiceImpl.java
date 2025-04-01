package com.leyuz.uc.log.mybatis.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyuz.common.mybatis.PageQuery;
import com.leyuz.uc.log.UserLogPO;
import com.leyuz.uc.log.mybatis.IUserLogService;
import com.leyuz.uc.log.mybatis.mapper.UserLogMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 用户日志服务实现类
 */
@Service
@RequiredArgsConstructor
public class UserLogServiceImpl extends ServiceImpl<UserLogMapper, UserLogPO> implements IUserLogService {

    @Override
    public Page<UserLogPO> queryUserLogs(PageQuery pageQuery) {
        Map<String, Object> params = pageQuery.getParams();
        Long userId = (Long) params.get("userId");
        String ipAddress = (String) params.get("ipAddress");
        Integer logType = (Integer) params.get("logType");

        LambdaQueryWrapper<UserLogPO> queryWrapper = Wrappers.lambdaQuery();

        if (userId != null && userId > 0) {
            queryWrapper.eq(UserLogPO::getCreateBy, userId);
        }

        if (StringUtils.isNotBlank(ipAddress)) {
            queryWrapper.eq(UserLogPO::getIpAddress, ipAddress);
        }

        if (logType != null) {
            queryWrapper.eq(UserLogPO::getLogType, logType);
        }

        queryWrapper.orderByDesc(UserLogPO::getLogId);

        return page(pageQuery.toPage(), queryWrapper);
    }
} 