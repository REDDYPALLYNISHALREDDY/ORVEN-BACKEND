package com.chat.ChatApplication.config;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks every active STOMP/WebSocket session.
 *
 * A user is online when at least one session is connected. This is important
 * for users signed in on multiple devices/tabs.
 */
@Component
public class SessionRegistry {

    private final Map<String, Long> sessions = new ConcurrentHashMap<>();

    private final Map<Long, Set<String>> userSessions =
            new ConcurrentHashMap<>();

    private final Map<Long, Object> userLocks =
            new ConcurrentHashMap<>();

    /**
     * @return true when this session is the user's first active session.
     */
    public boolean addSession(String sessionId, Long userId) {
        Object lock = userLocks.computeIfAbsent(userId, id -> new Object());

        synchronized (lock) {
            sessions.put(sessionId, userId);

            Set<String> set = userSessions.computeIfAbsent(
                    userId,
                    id -> ConcurrentHashMap.newKeySet()
            );

            boolean wasOffline = set.isEmpty();
            set.add(sessionId);

            return wasOffline;
        }
    }

    public Long removeSession(String sessionId) {
        Long userId = sessions.remove(sessionId);

        if (userId == null) {
            return null;
        }

        Object lock = userLocks.computeIfAbsent(userId, id -> new Object());

        synchronized (lock) {
            Set<String> set = userSessions.get(userId);

            if (set != null) {
                set.remove(sessionId);

                if (set.isEmpty()) {
                    userSessions.remove(userId);
                }
            }
        }

        return userId;
    }

    public Long getUserId(String sessionId) {
        return sessions.get(sessionId);
    }

    public boolean isUserOnline(Long userId) {
        Set<String> set = userSessions.get(userId);
        return set != null && !set.isEmpty();
    }

    /**
     * Executes the supplied action only while the user has no active session.
     *
     * The same per-user lock is used by addSession(), so a reconnect cannot
     * race with the delayed offline transition.
     */
    public void executeIfOffline(Long userId, Runnable action) {
        Object lock = userLocks.computeIfAbsent(userId, id -> new Object());

        synchronized (lock) {
            if (!isUserOnline(userId)) {
                action.run();
            }
        }
    }
}
