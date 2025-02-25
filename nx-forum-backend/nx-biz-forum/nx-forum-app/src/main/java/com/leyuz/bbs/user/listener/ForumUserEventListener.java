package com.leyuz.bbs.user.listener;

import com.leyuz.bbs.user.property.ForumUserPropertyMapper;
import com.leyuz.uc.user.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ForumUserEventListener {
    private final ForumUserPropertyMapper forumUserPropertyService;

    @EventListener
    public void handleUserRegisteredEvent(UserRegisteredEvent event) {
        forumUserPropertyService.initUserProperty(event.getUserId());
    }
}