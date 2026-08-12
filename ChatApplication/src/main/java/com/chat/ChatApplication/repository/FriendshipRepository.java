package com.chat.ChatApplication.repository;

import com.chat.ChatApplication.entity.Friendship;
import com.chat.ChatApplication.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository
        extends JpaRepository<Friendship, Long> {

    List<Friendship> findByUser1(User user);

    List<Friendship> findByUser2(User user);

    Optional<Friendship> findByUser1AndUser2(
            User user1,
            User user2
    );

    @Transactional
    void deleteByUser1(User user);

    @Transactional
    void deleteByUser2(User user);
}