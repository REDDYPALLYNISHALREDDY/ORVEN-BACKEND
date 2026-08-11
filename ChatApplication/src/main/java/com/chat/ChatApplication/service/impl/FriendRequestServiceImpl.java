package com.chat.ChatApplication.service.impl;

import com.chat.ChatApplication.dto.FriendRequestDto;
import com.chat.ChatApplication.entity.FriendRequest;
import com.chat.ChatApplication.entity.FriendRequestStatus;
import com.chat.ChatApplication.entity.Friendship;
import com.chat.ChatApplication.dto.FriendDto;
import com.chat.ChatApplication.entity.User;
import com.chat.ChatApplication.exception.ResourceAlreadyExistsException;
import com.chat.ChatApplication.exception.ResourceNotFoundException;
import com.chat.ChatApplication.repository.FriendRequestRepository;
import com.chat.ChatApplication.repository.UserRepository;
import com.chat.ChatApplication.service.FriendRequestService;
import com.chat.ChatApplication.repository.FriendshipRepository;

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
public class FriendRequestServiceImpl implements FriendRequestService {

    private final FriendRequestRepository friendRequestRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;

    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository conversationMemberRepository;

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));
    }

    private FriendRequestDto map(FriendRequest request) {

        return FriendRequestDto.builder()

                .id(request.getId())

                .senderId(request.getSender().getId())

                .senderName(request.getSender().getFullName())

                .senderEmail(request.getSender().getEmail())

                .senderProfileImage(
                        request.getSender().getProfileImage()
                )

                .receiverId(request.getReceiver().getId())

                .receiverName(request.getReceiver().getFullName())

                .status(request.getStatus().name())

                .build();

    }

    @Override
    public FriendRequestDto sendRequest(Long receiverId) {

        User sender = getCurrentUser();

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Receiver not found"));

        if (sender.getId().equals(receiver.getId())) {
            throw new RuntimeException("You cannot send a friend request to yourself.");
        }

        if (friendRequestRepository.findBySenderAndReceiver(sender, receiver).isPresent()) {
            throw new ResourceAlreadyExistsException("Friend request already sent.");
        }

        FriendRequest request = FriendRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .status(FriendRequestStatus.PENDING)
                .build();

        friendRequestRepository.save(request);

        return map(request);
    }

    @Override
    public FriendRequestDto acceptRequest(Long requestId) {

        User currentUser = getCurrentUser();

        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Friend request not found"));

        if (!request.getReceiver().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not allowed to accept this request.");
        }

        request.setStatus(FriendRequestStatus.ACCEPTED);

        friendRequestRepository.save(request);

        // Create Friendship
        Friendship friendship = Friendship.builder()
                .user1(request.getSender())
                .user2(request.getReceiver())
                .build();

        friendshipRepository.save(friendship);

        Conversation conversation = conversationRepository
                .findByType(ConversationType.PRIVATE)
                .stream()
                .filter(c -> {

                    List<ConversationMember> members =
                            conversationMemberRepository.findByConversation(c);

                    boolean senderExists = members.stream()
                            .anyMatch(m ->
                                    m.getUser().getId()
                                            .equals(request.getSender().getId()));

                    boolean receiverExists = members.stream()
                            .anyMatch(m ->
                                    m.getUser().getId()
                                            .equals(request.getReceiver().getId()));

                    return senderExists &&
                            receiverExists &&
                            members.size() == 2;

                })
                .findFirst()
                .orElse(null);

            if (conversation == null) {

            conversation = Conversation.builder()
                    .type(ConversationType.PRIVATE)
                    .build();

            conversationRepository.save(conversation);

            conversationMemberRepository.save(

                    ConversationMember.builder()
                            .conversation(conversation)
                            .user(request.getSender())
                            .build()

            );

            conversationMemberRepository.save(

                    ConversationMember.builder()
                            .conversation(conversation)
                            .user(request.getReceiver())
                            .build()

            );

        }
        return map(request);
    }

    @Override
    public FriendRequestDto rejectRequest(Long requestId) {

        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Friend request not found"));

        request.setStatus(FriendRequestStatus.REJECTED);

        friendRequestRepository.save(request);

        return map(request);
    }

    @Override
    public List<FriendRequestDto> pendingRequests() {

        User currentUser = getCurrentUser();

        return friendRequestRepository
                .findByReceiverAndStatus(currentUser, FriendRequestStatus.PENDING)
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    public List<FriendDto> getFriends() {

        User currentUser = getCurrentUser();

        List<FriendDto> friends = new java.util.ArrayList<>();

        friendshipRepository.findByUser1(currentUser).forEach(friendship ->
                friends.add(
                        FriendDto.builder()
                                .id(friendship.getUser2().getId())
                                .fullName(friendship.getUser2().getFullName())
                                .email(friendship.getUser2().getEmail())
                                .profileImage(friendship.getUser2().getProfileImage())
                                .build()
                ));

        friendshipRepository.findByUser2(currentUser).forEach(friendship ->
                friends.add(
                        FriendDto.builder()
                                .id(friendship.getUser1().getId())
                                .fullName(friendship.getUser1().getFullName())
                                .email(friendship.getUser1().getEmail())
                                .profileImage(friendship.getUser1().getProfileImage())
                                .build()
                ));

        return friends;
    }
}