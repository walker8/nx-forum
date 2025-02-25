package com.leyuz.uc.user.service;

import com.leyuz.common.exception.ValidationException;
import com.leyuz.uc.user.UserE;
import com.leyuz.uc.user.event.UserRegisteredEvent;
import com.leyuz.uc.user.gateway.UserGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDomainService {
    private final UserGateway userGateway;
    private final ApplicationEventPublisher eventPublisher;

    public void save(UserE userE) {
        userE.register();
        // 检查用户名是否已存在
        UserE existingUser = userGateway.getByUserName(userE.getUserName());
        if (existingUser != null) {
            throw new ValidationException("用户名已存在");
        }

        // 检查手机号是否已存在
        existingUser = userGateway.getByPhone(userE.getPhone());
        if (existingUser != null) {
            throw new ValidationException("手机号已被使用");
        }

        // 检查邮箱是否已存在
        existingUser = userGateway.getByEmail(userE.getEmail());
        if (existingUser != null) {
            throw new ValidationException("邮箱已被使用");
        }

        // 保存用户
        userGateway.save(userE);

        // 发布用户注册成功事件
        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .userId(userE.getUserId())
                .userName(userE.getUserName())
                .build();
        eventPublisher.publishEvent(event);
    }

    public void update(UserE userE) {
        userE.update();
        // 检查用户是否存在
        UserE existingUser = userGateway.getById(userE.getUserId());
        if (existingUser == null) {
            throw new ValidationException("用户不存在");
        }

        // 检查用户名是否已被其他用户使用
        if (userE.getUserName() != null) {
            existingUser = userGateway.getByUserName(userE.getUserName());
            if (existingUser != null && !existingUser.getUserId().equals(userE.getUserId())) {
                throw new ValidationException("用户名已存在");
            }
        }

        // 检查手机号是否已被其他用户使用
        if (userE.getPhone() != null) {
            existingUser = userGateway.getByPhone(userE.getPhone());
            if (existingUser != null && !existingUser.getUserId().equals(userE.getUserId())) {
                throw new ValidationException("手机号已被使用");
            }
        }

        // 检查邮箱是否已被其他用户使用
        if (userE.getEmail() != null) {
            existingUser = userGateway.getByEmail(userE.getEmail());
            if (existingUser != null && !existingUser.getUserId().equals(userE.getUserId())) {
                throw new ValidationException("邮箱已被使用");
            }
        }

        userGateway.update(userE);
    }

    public void checkUserName(String username) {
        UserE userE = UserE.builder().userName(username).build();
        userE.checkUserNameNotEmpty();
        userE.checkUserName();

        UserE existingUser = userGateway.getByUserName(username);
        if (existingUser != null) {
            throw new ValidationException("用户名已存在");
        }
    }
}
