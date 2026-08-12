package com.chat.ChatApplication.service.impl;

import com.chat.ChatApplication.dto.FriendRequestDto;
import com.chat.ChatApplication.dto.FriendDto;

import com.chat.ChatApplication.entity.FriendRequest;
import com.chat.ChatApplication.entity.FriendRequestStatus;
import com.chat.ChatApplication.entity.Friendship;
import com.chat.ChatApplication.entity.User;

import com.chat.ChatApplication.exception.ResourceAlreadyExistsException;
import com.chat.ChatApplication.exception.ResourceNotFoundException;

import com.chat.ChatApplication.repository.FriendRequestRepository;
import com.chat.ChatApplication.repository.UserRepository;
import com.chat.ChatApplication.repository.FriendshipRepository;

import com.chat.ChatApplication.service.FriendRequestService;

import com.chat.ChatApplication.entity.Conversation;
import com.chat.ChatApplication.entity.ConversationMember;
import com.chat.ChatApplication.entity.ConversationType;

import com.chat.ChatApplication.repository.ConversationMemberRepository;
import com.chat.ChatApplication.repository.ConversationRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendRequestServiceImpl
        implements FriendRequestService {

    private final FriendRequestRepository friendRequestRepository;

    private final UserRepository userRepository;

    private final FriendshipRepository friendshipRepository;

    private final ConversationRepository conversationRepository;

    private final ConversationMemberRepository conversationMemberRepository;


    // =========================================================
    // CURRENT USER
    // =========================================================

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        return userRepository
                .findByEmail(
                        authentication.getName()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );
    }


    // =========================================================
    // MAP
    // =========================================================

    private FriendRequestDto map(
            FriendRequest request
    ) {

        return FriendRequestDto.builder()

                .id(
                        request.getId()
                )

                .senderId(
                        request
                                .getSender()
                                .getId()
                )

                .senderName(
                        request
                                .getSender()
                                .getFullName()
                )

                .senderEmail(
                        request
                                .getSender()
                                .getEmail()
                )

                .senderProfileImage(
                        request
                                .getSender()
                                .getProfileImage()
                )

                .receiverId(
                        request
                                .getReceiver()
                                .getId()
                )

                .receiverName(
                        request
                                .getReceiver()
                                .getFullName()
                )

                .status(
                        request
                                .getStatus()
                                .name()
                )

                .build();
    }


    // =========================================================
    // SEND REQUEST
    // =========================================================

    @Override
    public FriendRequestDto sendRequest(
            Long receiverId
    ) {

        User sender =
                getCurrentUser();


        User receiver =
                userRepository
                        .findById(receiverId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Receiver not found"
                                )
                        );


        if (
                sender.getId()
                        .equals(
                                receiver.getId()
                        )
        ) {

            throw new RuntimeException(
                    "You cannot send a friend request to yourself."
            );

        }


        // -----------------------------------------------------
        // Already friends?
        // -----------------------------------------------------

        boolean alreadyFriends =
                friendshipRepository
                        .findByUser1AndUser2(
                                sender,
                                receiver
                        )
                        .isPresent()
                        ||
                        friendshipRepository
                                .findByUser1AndUser2(
                                        receiver,
                                        sender
                                )
                                .isPresent();


        if (alreadyFriends) {

            throw new ResourceAlreadyExistsException(
                    "You are already friends."
            );

        }


        // -----------------------------------------------------
        // Existing request sent by current user
        // -----------------------------------------------------

        FriendRequest existingSent =
                friendRequestRepository
                        .findBySenderAndReceiver(
                                sender,
                                receiver
                        )
                        .orElse(null);


        if (existingSent != null) {

            if (
                    existingSent.getStatus()
                            == FriendRequestStatus.PENDING
            ) {

                throw new ResourceAlreadyExistsException(
                        "Friend request already sent."
                );

            }


            /*
             * Previously rejected.
             *
             * Allow sending again.
             */

            if (
                    existingSent.getStatus()
                            == FriendRequestStatus.REJECTED
            ) {

                existingSent.setStatus(
                        FriendRequestStatus.PENDING
                );

                friendRequestRepository.save(
                        existingSent
                );

                return map(
                        existingSent
                );

            }

        }


        // -----------------------------------------------------
        // Existing incoming request
        // -----------------------------------------------------

        FriendRequest existingIncoming =
                friendRequestRepository
                        .findBySenderAndReceiver(
                                receiver,
                                sender
                        )
                        .orElse(null);


        if (
                existingIncoming != null
                        &&
                        existingIncoming.getStatus()
                                == FriendRequestStatus.PENDING
        ) {

            throw new ResourceAlreadyExistsException(
                    "This user has already sent you a friend request."
            );

        }


        // -----------------------------------------------------
        // New request
        // -----------------------------------------------------

        FriendRequest request =
                FriendRequest.builder()

                        .sender(sender)

                        .receiver(receiver)

                        .status(
                                FriendRequestStatus.PENDING
                        )

                        .build();


        friendRequestRepository.save(
                request
        );


        return map(
                request
        );
    }


    // =========================================================
    // ACCEPT
    // =========================================================

    @Override
    public FriendRequestDto acceptRequest(
            Long requestId
    ) {

        User currentUser =
                getCurrentUser();


        FriendRequest request =
                friendRequestRepository
                        .findById(requestId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Friend request not found"
                                )
                        );


        /*
         * Only receiver can accept.
         */

        if (
                !request
                        .getReceiver()
                        .getId()
                        .equals(
                                currentUser.getId()
                        )
        ) {

            throw new RuntimeException(
                    "You are not allowed to accept this request."
            );

        }


        if (
                request.getStatus()
                        != FriendRequestStatus.PENDING
        ) {

            throw new RuntimeException(
                    "This friend request is no longer pending."
            );

        }


        request.setStatus(
                FriendRequestStatus.ACCEPTED
        );

        friendRequestRepository.save(
                request
        );


        // -----------------------------------------------------
        // Create friendship only if it doesn't already exist
        // -----------------------------------------------------

        boolean friendshipExists =
                friendshipRepository
                        .findByUser1AndUser2(
                                request.getSender(),
                                request.getReceiver()
                        )
                        .isPresent()
                        ||
                        friendshipRepository
                                .findByUser1AndUser2(
                                        request.getReceiver(),
                                        request.getSender()
                                )
                                .isPresent();


        if (!friendshipExists) {

            Friendship friendship =
                    Friendship.builder()

                            .user1(
                                    request.getSender()
                            )

                            .user2(
                                    request.getReceiver()
                            )

                            .build();


            friendshipRepository.save(
                    friendship
            );

        }


        // -----------------------------------------------------
        // Create private conversation if necessary
        // -----------------------------------------------------

        Conversation conversation =
                conversationRepository
                        .findByType(
                                ConversationType.PRIVATE
                        )
                        .stream()

                        .filter(c -> {

                            List<ConversationMember>
                                    members =
                                    conversationMemberRepository
                                            .findByConversation(c);


                            boolean senderExists =
                                    members
                                            .stream()
                                            .anyMatch(
                                                    member ->
                                                            member
                                                                    .getUser()
                                                                    .getId()
                                                                    .equals(
                                                                            request
                                                                                    .getSender()
                                                                                    .getId()
                                                                    )
                                            );


                            boolean receiverExists =
                                    members
                                            .stream()
                                            .anyMatch(
                                                    member ->
                                                            member
                                                                    .getUser()
                                                                    .getId()
                                                                    .equals(
                                                                            request
                                                                                    .getReceiver()
                                                                                    .getId()
                                                                    )
                                            );


                            return senderExists
                                    &&
                                    receiverExists
                                    &&
                                    members.size() == 2;

                        })

                        .findFirst()
                        .orElse(null);


        if (conversation == null) {

            conversation =
                    Conversation.builder()

                            .type(
                                    ConversationType.PRIVATE
                            )

                            .build();


            conversationRepository.save(
                    conversation
            );


            conversationMemberRepository.save(

                    ConversationMember.builder()

                            .conversation(
                                    conversation
                            )

                            .user(
                                    request.getSender()
                            )

                            .build()

            );


            conversationMemberRepository.save(

                    ConversationMember.builder()

                            .conversation(
                                    conversation
                            )

                            .user(
                                    request.getReceiver()
                            )

                            .build()

            );

        }


        return map(
                request
        );
    }


    // =========================================================
    // REJECT
    // =========================================================

    @Override
    public FriendRequestDto rejectRequest(
            Long requestId
    ) {

        User currentUser =
                getCurrentUser();


        FriendRequest request =
                friendRequestRepository
                        .findById(requestId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Friend request not found"
                                )
                        );


        /*
         * Only receiver can reject.
         */

        if (
                !request
                        .getReceiver()
                        .getId()
                        .equals(
                                currentUser.getId()
                        )
        ) {

            throw new RuntimeException(
                    "You are not allowed to reject this request."
            );

        }


        if (
                request.getStatus()
                        != FriendRequestStatus.PENDING
        ) {

            throw new RuntimeException(
                    "This friend request is no longer pending."
            );

        }


        request.setStatus(
                FriendRequestStatus.REJECTED
        );


        friendRequestRepository.save(
                request
        );


        return map(
                request
        );
    }


    // =========================================================
    // PENDING REQUESTS
    // =========================================================

    @Override
    public List<FriendRequestDto> pendingRequests() {

        User currentUser =
                getCurrentUser();


        return friendRequestRepository

                .findByReceiverAndStatus(
                        currentUser,
                        FriendRequestStatus.PENDING
                )

                .stream()

                .map(
                        this::map
                )

                .toList();
    }


    // =========================================================
    // FRIENDS
    // =========================================================

    @Override
    public List<FriendDto> getFriends() {

        User currentUser =
                getCurrentUser();


        List<FriendDto> friends =
                new java.util.ArrayList<>();


        friendshipRepository
                .findByUser1(currentUser)
                .forEach(
                        friendship ->
                                friends.add(

                                        FriendDto.builder()

                                                .id(
                                                        friendship
                                                                .getUser2()
                                                                .getId()
                                                )

                                                .fullName(
                                                        friendship
                                                                .getUser2()
                                                                .getFullName()
                                                )

                                                .email(
                                                        friendship
                                                                .getUser2()
                                                                .getEmail()
                                                )

                                                .profileImage(
                                                        friendship
                                                                .getUser2()
                                                                .getProfileImage()
                                                )

                                                .build()

                                )
                );


        friendshipRepository
                .findByUser2(currentUser)
                .forEach(
                        friendship ->
                                friends.add(

                                        FriendDto.builder()

                                                .id(
                                                        friendship
                                                                .getUser1()
                                                                .getId()
                                                )

                                                .fullName(
                                                        friendship
                                                                .getUser1()
                                                                .getFullName()
                                                )

                                                .email(
                                                        friendship
                                                                .getUser1()
                                                                .getEmail()
                                                )

                                                .profileImage(
                                                        friendship
                                                                .getUser1()
                                                                .getProfileImage()
                                                )

                                                .build()

                                )
                );


        return friends;
    }


    // =========================================================
    // STATUS
    // =========================================================

    @Override
    public String getStatus(
            Long userId
    ) {

        User currentUser =
                getCurrentUser();


        User otherUser =
                userRepository
                        .findById(userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"
                                )
                        );


        if (
                currentUser
                        .getId()
                        .equals(
                                otherUser.getId()
                        )
        ) {

            return "SELF";

        }


        // -----------------------------------------------------
        // Already friends
        // -----------------------------------------------------

        boolean friends =
                friendshipRepository
                        .findByUser1AndUser2(
                                currentUser,
                                otherUser
                        )
                        .isPresent()
                        ||
                        friendshipRepository
                                .findByUser1AndUser2(
                                        otherUser,
                                        currentUser
                                )
                                .isPresent();


        if (friends) {

            return "ACCEPTED";

        }


        // -----------------------------------------------------
        // Current user sent request
        // -----------------------------------------------------

        FriendRequest sent =
                friendRequestRepository
                        .findBySenderAndReceiver(
                                currentUser,
                                otherUser
                        )
                        .orElse(null);


        if (sent != null) {

            return sent
                    .getStatus()
                    .name();

        }


        // -----------------------------------------------------
        // Other user sent request
        // -----------------------------------------------------

        FriendRequest received =
                friendRequestRepository
                        .findBySenderAndReceiver(
                                otherUser,
                                currentUser
                        )
                        .orElse(null);


        if (received != null) {

            if (
                    received.getStatus()
                            == FriendRequestStatus.PENDING
            ) {

                return "INCOMING";

            }

            return received
                    .getStatus()
                    .name();

        }


        return "NONE";
    }
}