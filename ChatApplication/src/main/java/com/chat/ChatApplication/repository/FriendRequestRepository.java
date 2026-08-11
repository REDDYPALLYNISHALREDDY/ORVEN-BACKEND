package com.chat.ChatApplication.repository;

import com.chat.ChatApplication.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository
        extends JpaRepository<FriendRequest,Long> {

    List<FriendRequest> findByReceiverAndStatus(
            User receiver,
            FriendRequestStatus status);

    List<FriendRequest> findBySender(User sender);

    Optional<FriendRequest> findBySenderAndReceiver(
            User sender,
            User receiver);

}