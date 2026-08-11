package com.chat.ChatApplication.repository;

import com.chat.ChatApplication.entity.Conversation;
import com.chat.ChatApplication.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import com.chat.ChatApplication.entity.MessageStatus;
import com.chat.ChatApplication.entity.User;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByConversationOrderByCreatedAtAsc(Conversation conversation);

    Optional<Message> findTopByConversationOrderByCreatedAtDesc(Conversation conversation);

    List<Message> findByConversationAndStatusOrderByCreatedAtAsc(
            Conversation conversation,
            MessageStatus status
    );

    List<Message> findByConversationAndSenderNotAndStatus(
            Conversation conversation,
            User sender,
            MessageStatus status
    );

    @Transactional
    void deleteByConversation(Conversation conversation);

    long countBySenderAndFileUrlIsNotNull(User sender);

    List<Message> findBySenderAndFileUrlIsNotNull(User sender);

}