package com.leyuz.uc.log;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.leyuz.uc.domain.log.UserLogE;
import com.leyuz.uc.domain.log.dataobject.LogTypeV;
import com.leyuz.uc.domain.log.dataobject.OperationStatusV;
import com.leyuz.uc.domain.log.gateway.UserLogGateway;
import com.leyuz.uc.log.mybatis.IUserLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户日志网关实现类
 */
@Component
@RequiredArgsConstructor
public class UserLogGatewayImpl implements UserLogGateway {

    private final IUserLogService userLogService;

    @Override
    public void save(UserLogE userLogE) {
        UserLogPO userLogPO = convertToPO(userLogE);
        userLogService.save(userLogPO);
    }

    @Override
    public UserLogE getById(Long logId) {
        UserLogPO userLogPO = userLogService.getById(logId);
        return convertToE(userLogPO);
    }

    @Override
    public List<UserLogE> getByUserId(Long userId) {
        LambdaQueryWrapper<UserLogPO> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(UserLogPO::getCreateBy, userId);
        List<UserLogPO> userLogPOList = userLogService.list(queryWrapper);
        return userLogPOList.stream().map(this::convertToE).collect(Collectors.toList());
    }

    /**
     * 将领域实体转换为持久化对象
     */
    private UserLogPO convertToPO(UserLogE userLogE) {
        if (userLogE == null) {
            return null;
        }

        UserLogPO userLogPO = new UserLogPO();
        userLogPO.setLogId(userLogE.getLogId());
        userLogPO.setLogType(userLogE.getLogType().getCode());
        userLogPO.setLogContent(userLogE.getLogContent());
        userLogPO.setIpAddress(userLogE.getIpAddress());
        userLogPO.setUserAgent(userLogE.getUserAgent());
        userLogPO.setOperationStatus(userLogE.getOperationStatus().getCode());
        userLogPO.setCreateBy(userLogE.getCreateBy());
        userLogPO.setCreateTime(userLogE.getCreateTime());

        return userLogPO;
    }

    /**
     * 将持久化对象转换为领域实体
     */
    private UserLogE convertToE(UserLogPO userLogPO) {
        if (userLogPO == null) {
            return null;
        }

        return UserLogE.builder()
                .logId(userLogPO.getLogId())
                .logType(LogTypeV.fromCode(userLogPO.getLogType()))
                .logContent(userLogPO.getLogContent())
                .ipAddress(userLogPO.getIpAddress())
                .userAgent(userLogPO.getUserAgent())
                .operationStatus(OperationStatusV.fromCode(userLogPO.getOperationStatus()))
                .createBy(userLogPO.getCreateBy())
                .createTime(userLogPO.getCreateTime())
                .build();
    }
} 