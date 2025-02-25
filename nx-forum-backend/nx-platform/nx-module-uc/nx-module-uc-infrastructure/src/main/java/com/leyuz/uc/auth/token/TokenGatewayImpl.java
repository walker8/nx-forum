package com.leyuz.uc.auth.token;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.leyuz.common.utils.BaseEntityUtils;
import com.leyuz.common.utils.HeaderUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class TokenGatewayImpl implements TokenGateway {
    private final UserLoginTokenMapper userLoginTokenMapper;

    @Override
    public void saveToken(Long userId, String token) {
        // 删除原来的设备
        String deviceId = HeaderUtils.getDeviceId();
        if (StringUtils.isNotBlank(deviceId)) {
            deleteByDeviceId(deviceId, userId);
        }
        UserLoginTokenPO userLoginTokenPO = new UserLoginTokenPO();
        BaseEntityUtils.setCreateBaseEntity(userLoginTokenPO);
        userLoginTokenPO.setLoginIp(HeaderUtils.getIp());
        userLoginTokenPO.setUserAgent(HeaderUtils.getUserAgent());
        userLoginTokenPO.setCreateBy(userId);
        userLoginTokenPO.setUpdateBy(userId);
        userLoginTokenPO.setToken(token);
        userLoginTokenPO.setTokenExpiresAt(LocalDateTime.now().plusYears(1));
        userLoginTokenPO.setDeviceId(deviceId);
        userLoginTokenMapper.insert(userLoginTokenPO);
    }

    @Override
    public Long findUserIdByToken(String token) {
        QueryWrapper<UserLoginTokenPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("token", token);
        queryWrapper.eq("is_deleted", false);
        UserLoginTokenPO userLoginTokenPO = userLoginTokenMapper.selectOne(queryWrapper);
        if (userLoginTokenPO != null
                && userLoginTokenPO.getTokenExpiresAt().isAfter(LocalDateTime.now())) {
            return userLoginTokenPO.getCreateBy();
        }
        return 0L;
    }

    @Override
    public boolean deleteByToken(String token) {
        QueryWrapper<UserLoginTokenPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("token", token);
        UserLoginTokenPO userLoginTokenPO = new UserLoginTokenPO();
        BaseEntityUtils.setUpdateBaseEntity(userLoginTokenPO);
        userLoginTokenPO.setIsDeleted(true);
        return userLoginTokenMapper.update(userLoginTokenPO, queryWrapper) > 0;
    }

    @Override
    public boolean deleteByDeviceId(String deviceId, Long userId) {
        QueryWrapper<UserLoginTokenPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("device_id", deviceId);
        queryWrapper.eq("create_by", userId);
        UserLoginTokenPO userLoginTokenPO = new UserLoginTokenPO();
        BaseEntityUtils.setUpdateBaseEntity(userLoginTokenPO);
        userLoginTokenPO.setIsDeleted(true);
        return userLoginTokenMapper.update(userLoginTokenPO, queryWrapper) > 0;
    }
}
