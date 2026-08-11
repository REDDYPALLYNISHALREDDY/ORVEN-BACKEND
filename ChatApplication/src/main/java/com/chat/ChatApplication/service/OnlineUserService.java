package com.chat.ChatApplication.service;

import com.chat.ChatApplication.entity.User;
import com.chat.ChatApplication.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class OnlineUserService {

    private final UserRepository userRepository;

    private final Set<Long> onlineUsers =
            ConcurrentHashMap.newKeySet();

    public Set<Long> getOnlineUsers(){

        return onlineUsers;

    }

    public void userConnected(Long userId) {

        System.out.println("CONNECTED : " + userId);

        onlineUsers.add(userId);

        userRepository.findById(userId).ifPresent(user -> {

            user.setOnline(true);

            userRepository.save(user);

            System.out.println("📡 STATUS SENT : ONLINE -> " + user.getId());

        });

    }

    public void userDisconnected(Long userId) {

        onlineUsers.remove(userId);

        userRepository.findById(userId).ifPresent(user -> {

            user.setOnline(false);

            user.setLastSeen(LocalDateTime.now());

            userRepository.save(user);

            System.out.println("📡 STATUS SENT : OFFLINE -> " + user.getId());

        });

    }

    public boolean isOnline(Long userId) {

        return onlineUsers.contains(userId);

    }

    public User getUser(Long userId){

        return userRepository.findById(userId)
                .orElse(null);

    }

}