package com.leyuz.uc.log.listener;

import com.leyuz.uc.log.UserLogApplication;
import com.leyuz.uc.log.dto.LogTypeV;
import com.leyuz.uc.log.dto.OperationStatusV;
import com.leyuz.uc.user.event.UserLoginEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * 用户日志事件监听器
 * 负责处理用户日志相关事件
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserLogEventListener {

    private final UserLogApplication userLogApplication;

    /**
     * 处理用户登录事件，记录相关日志
     */
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleUserLoginEvent(UserLoginEvent event) {
        // 记录用户操作日志
        userLogApplication.recordLog(
                event.getUserId(),
                Objects.requireNonNull(LogTypeV.fromCode(event.getLogType())),
                event.getLogContent(),
                event.getIpAddress(),
                event.getUserAgent(),
                Objects.requireNonNull(OperationStatusV.fromCode(event.getOperationStatus()))
        );
    }
} 