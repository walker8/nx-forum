package com.leyuz.uc.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.leyuz.common.utils.BaseEntityUtils;
import com.leyuz.uc.domain.user.UserE;
import com.leyuz.uc.domain.user.dataobject.UserStatusV;
import com.leyuz.uc.domain.user.gateway.UserGateway;
import com.leyuz.uc.user.mybatis.IUserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserGatewayImpl implements UserGateway {
    private final IUserService userService;
    private final PasswordEncoder encoder;

    @Override
    public UserE save(UserE userE) {
        if (userE != null) {
            UserPO userPO = new UserPO();
            userPO.setUserName(userE.getUserName());
            userPO.setEmail(userE.getEmail());
            userPO.setPhone(userE.getPhone());
            if (StringUtils.isNotEmpty(userE.getPassword())) {
                userPO.setPassword(encoder.encode(userE.getPassword()));
            }
            userPO.setAvatar(userE.getAvatar());
            userPO.setLoginIp(userE.getLoginIp());
            userPO.setLoginDate(userE.getLoginDate());
            userPO.setAccountStatus((byte) userE.getStatus().getValue());
            BaseEntityUtils.setCreateBaseEntity(userPO);
            userService.save(userPO);
            userE.setUserId(userPO.getUserId());
            return userE;
        }
        return null;
    }

    @Override
    public void update(UserE userE) {
        if (userE == null) {
            return;
        }
        UserPO userPO = new UserPO();
        if (StringUtils.isNotEmpty(userE.getUserName())) {
            userPO.setUserName(userE.getUserName());
        }
        if (StringUtils.isNotEmpty(userE.getEmail())) {
            userPO.setEmail(userE.getEmail());
        }
        if (StringUtils.isNotEmpty(userE.getPhone())) {
            userPO.setPhone(userE.getPhone());
        }
        if (StringUtils.isNotEmpty(userE.getPassword())) {
            userPO.setPassword(encoder.encode(userE.getPassword()));
        }
        if (StringUtils.isNotEmpty(userE.getAvatar())) {
            userPO.setAvatar(userE.getAvatar());
        }
        if (StringUtils.isNotEmpty(userE.getIntro())) {
            userPO.setIntro(userE.getIntro());
        }
        userPO.setUserId(userE.getUserId());
        BaseEntityUtils.setUpdateBaseEntity(userPO);
        userService.updateById(userPO);
    }

    @Override
    public UserE getById(long userId) {
        UserPO userPO = userService.getById(userId);
        return convert(userPO);
    }

    @Override
    public UserE getByUserName(String userName) {
        if (StringUtils.isEmpty(userName)) {
            return null;
        }
        QueryWrapper<UserPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_name", userName);
        queryWrapper.eq("is_deleted", false);
        UserPO userPO = userService.getOne(queryWrapper);
        return convert(userPO);
    }

    @Override
    public UserE getByPhone(String phone) {
        if (StringUtils.isEmpty(phone)) {
            return null;
        }
        QueryWrapper<UserPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone", phone);
        queryWrapper.eq("is_deleted", false);
        UserPO userPO = userService.getOne(queryWrapper);
        return convert(userPO);
    }

    @Override
    public UserE getByEmail(String email) {
        if (StringUtils.isEmpty(email)) {
            return null;
        }
        QueryWrapper<UserPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        queryWrapper.eq("is_deleted", false);
        UserPO userPO = userService.getOne(queryWrapper);
        return convert(userPO);
    }

    private UserE convert(UserPO userPO) {
        if (userPO != null) {
            return UserE.builder()
                    .userId(userPO.getUserId())
                    .userName(userPO.getUserName())
                    .phone(userPO.getPhone())
                    .email(userPO.getEmail())
                    .avatar(userPO.getAvatar())
                    .intro(userPO.getIntro())
                    .status(UserStatusV.of(userPO.getAccountStatus()))
                    .createTime(userPO.getCreateTime())
                    // 密码校验时使用
                    .password(userPO.getPassword())
                    .build();
        }
        return null;
    }

    @Override
    public void deleteById(Long userId) {
        UserPO userPO = new UserPO();
        userPO.setUserId(userId);
        userPO.setIsDeleted(true);
        BaseEntityUtils.setUpdateBaseEntity(userPO);
        userService.updateById(userPO);
    }
}
