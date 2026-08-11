package com.chat.ChatApplication.repository;

import com.chat.ChatApplication.entity.Friendship;
import com.chat.ChatApplication.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    List<Friendship> findByUser1(User user);

    List<Friendship> findByUser2(User user);

    Optional<Friendship> findByUser1AndUser2(User user1, User user2);

}