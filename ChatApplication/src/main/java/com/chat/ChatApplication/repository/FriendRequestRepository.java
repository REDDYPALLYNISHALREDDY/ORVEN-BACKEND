package com.chat.ChatApplication.repository;

import com.chat.ChatApplication.entity.FriendRequest;
import com.chat.ChatApplication.entity.FriendRequestStatus;
import com.chat.ChatApplication.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository
        extends JpaRepository<FriendRequest, Long> {

    List<FriendRequest> findByReceiverAndStatus(
            User receiver,
            FriendRequestStatus status
    );

    List<FriendRequest> findBySender(
            User sender
    );

    Optional<FriendRequest> findBySenderAndReceiver(
            User sender,
            User receiver
    );

    @Transactional
    void deleteBySender(User sender);

    @Transactional
    void deleteByReceiver(User receiver);
}