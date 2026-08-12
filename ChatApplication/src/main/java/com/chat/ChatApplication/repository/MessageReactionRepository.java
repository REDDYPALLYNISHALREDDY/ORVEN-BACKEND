package com.chat.ChatApplication.repository;

import com.chat.ChatApplication.entity.Message;
import com.chat.ChatApplication.entity.MessageReaction;
import com.chat.ChatApplication.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface MessageReactionRepository
        extends JpaRepository<MessageReaction, Long> {

    Optional<MessageReaction> findByMessageAndUser(
            Message message,
            User user
    );

    List<MessageReaction> findByMessage(
            Message message
    );

    @Transactional
    void deleteByMessageAndUser(
            Message message,
            User user
    );

    @Transactional
    void deleteByUser(
            User user
    );
}