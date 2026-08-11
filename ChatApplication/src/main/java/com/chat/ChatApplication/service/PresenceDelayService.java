package com.chat.ChatApplication.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Debounces WebSocket disconnects.
 */
@Service
public class PresenceDelayService {

    private static final long OFFLINE_DELAY_SECONDS = 3;

    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);

    private final ConcurrentHashMap<Long, ScheduledFuture<?>> tasks =
            new ConcurrentHashMap<>();

    public void schedule(Long userId, Runnable task) {
        cancel(userId);

        ScheduledFuture<?> future = scheduler.schedule(
                () -> {
                    try {
                        task.run();
                    } finally {
                        tasks.remove(userId);
                    }
                },
                OFFLINE_DELAY_SECONDS,
                TimeUnit.SECONDS
        );

        tasks.put(userId, future);
    }

    public void cancel(Long userId) {
        ScheduledFuture<?> future = tasks.remove(userId);

        if (future != null) {
            future.cancel(false);
        }
    }
}
