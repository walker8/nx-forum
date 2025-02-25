package com.leyuz.uc.user.listener;

import com.leyuz.uc.domain.user.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventListener {

    @EventListener
    public void handleUserRegisteredEvent(UserRegisteredEvent event) {
        log.info("用户注册成功：userId={}, userName={}", event.getUserId(), event.getUserName());

        // TODO: 在这里添加用户注册成功后的处理逻辑
        // 1. 发送欢迎邮件
        // 2. 发送欢迎短信
        // 3. 初始化用户的其他数据
        // 4. 记录用户注册日志
        // 等等...
    }
} 