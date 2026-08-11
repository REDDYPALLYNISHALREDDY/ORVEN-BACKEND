package com.chat.ChatApplication.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserConnectedEvent extends ApplicationEvent {

    private final Long userId;

    public UserConnectedEvent(Object source, Long userId) {
        super(source);
        this.userId = userId;
    }
}