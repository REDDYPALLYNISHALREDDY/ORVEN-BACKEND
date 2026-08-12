package com.chat.ChatApplication.service.impl;

import com.chat.ChatApplication.dto.MessageDto;
import com.chat.ChatApplication.dto.ReactionRequest;
import com.chat.ChatApplication.entity.Message;
import com.chat.ChatApplication.entity.MessageReaction;
import com.chat.ChatApplication.entity.User;
import com.chat.ChatApplication.exception.ResourceNotFoundException;
import com.chat.ChatApplication.repository.MessageReactionRepository;
import com.chat.ChatApplication.repository.MessageRepository;
import com.chat.ChatApplication.repository.UserRepository;
import com.chat.ChatApplication.service.ReactionService;

import lombok.RequiredArgsConstructor;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class ReactionServiceImpl
        implements ReactionService {


    private final MessageRepository messageRepository;

    private final MessageReactionRepository reactionRepository;

    private final UserRepository userRepository;

    private final SimpMessagingTemplate messagingTemplate;


    /*
     * =====================================================
     * CURRENT USER
     * =====================================================
     */

    private User currentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        return userRepository
                .findByEmail(
                        authentication.getName()
                )
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "User not found"
                                )
                );
    }


    /*
     * =====================================================
     * REACT / REMOVE / CHANGE REACTION
     * =====================================================
     */

    @Override
    public MessageDto reactToMessage(
            Long messageId,
            ReactionRequest request
    ) {

        User me =
                currentUser();


        Message message =
                messageRepository
                        .findById(messageId)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Message not found"
                                        )
                        );


        Optional<MessageReaction>
                existingReaction =
                reactionRepository
                        .findByMessageAndUser(
                                message,
                                me
                        );


        /*
         * =================================================
         * EXISTING REACTION
         * =================================================
         */

        if (
                existingReaction.isPresent()
        ) {

            MessageReaction reaction =
                    existingReaction.get();


            /*
             * Same emoji clicked again
             *
             * Example:
             *
             * 👍  → click 👍 → remove
             */

            if (
                    reaction.getEmoji()
                            .equals(
                                    request.getEmoji()
                            )
            ) {

                reactionRepository.delete(
                        reaction
                );

            }


            /*
             * Different emoji
             *
             * Example:
             *
             * 👍 → ❤️
             */

            else {

                reaction.setEmoji(
                        request.getEmoji()
                );

                reactionRepository.save(
                        reaction
                );

            }

        }


        /*
         * =================================================
         * NEW REACTION
         * =================================================
         */

        else {

            MessageReaction reaction =
                    MessageReaction.builder()
                            .message(message)
                            .user(me)
                            .emoji(
                                    request.getEmoji()
                            )
                            .build();

            reactionRepository.save(
                    reaction
            );

        }


        /*
         * Reload message after modification.
         */

        message =
                messageRepository
                        .findById(messageId)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Message not found"
                                        )
                        );


        /*
         * Build COMPLETE message DTO.
         *
         * This is important.
         *
         * The WebSocket event must contain the
         * original text/image/file/audio information
         * as well as the reaction information.
         */

        MessageDto dto =
                buildMessageDto(
                        message,
                        me
                );


        /*
         * Broadcast complete message.
         */

        messagingTemplate.convertAndSend(
                "/topic/chat/" +
                        message
                                .getConversation()
                                .getId(),
                dto
        );


        return dto;

    }


    /*
     * =====================================================
     * BUILD COMPLETE MESSAGE DTO
     * =====================================================
     */

    private MessageDto buildMessageDto(
            Message message,
            User me
    ) {

        MessageDto dto =
                new MessageDto();


        /*
         * Basic message information
         */

        dto.setId(
                message.getId()
        );

        dto.setConversationId(
                message
                        .getConversation()
                        .getId()
        );

        dto.setSenderId(
                message
                        .getSender()
                        .getId()
        );

        dto.setSenderName(
                message
                        .getSender()
                        .getFullName()
        );

        dto.setContent(
                message.getContent()
        );

        dto.setCreatedAt(
                message.getCreatedAt()
        );

        dto.setStatus(
                message.getStatus()
        );

        dto.setType(
                message.getType()
        );

        dto.setEdited(
                message.isEdited()
        );

        dto.setStarred(
                message.isStarred()
        );


        /*
         * =================================================
         * IMPORTANT:
         * FILE / IMAGE / AUDIO DATA
         * =================================================
         */

        dto.setFileName(
                message.getFileName()
        );

        dto.setFileUrl(
                message.getFileUrl()
        );

        dto.setFileType(
                message.getFileType()
        );

        dto.setFileSize(
                message.getFileSize()
        );


        /*
         * =================================================
         * REPLY INFORMATION
         * =================================================
         */

        if (
                message.getReplyTo() != null
        ) {

            dto.setReplyToId(
                    message
                            .getReplyTo()
                            .getId()
            );

            dto.setReplyToContent(
                    message
                            .getReplyTo()
                            .getContent()
            );

            dto.setReplyToSenderName(
                    message
                            .getReplyTo()
                            .getSender()
                            .getFullName()
            );

        }


        /*
         * =================================================
         * REACTION COUNTS
         * =================================================
         */

        Map<String, Integer>
                counts =
                new HashMap<>();


        for (
                MessageReaction reaction :
                message.getReactions()
        ) {

            counts.merge(
                    reaction.getEmoji(),
                    1,
                    Integer::sum
            );

        }


        dto.setReactions(
                counts
        );


        /*
         * =================================================
         * CURRENT USER'S REACTION
         * =================================================
         */

        message
                .getReactions()
                .stream()
                .filter(
                        reaction ->
                                reaction
                                        .getUser()
                                        .getId()
                                        .equals(
                                                me.getId()
                                        )
                )
                .findFirst()
                .ifPresent(
                        reaction ->
                                dto.setMyReaction(
                                        reaction.getEmoji()
                                )
                );


        return dto;

    }

}