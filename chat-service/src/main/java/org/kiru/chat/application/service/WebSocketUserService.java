package org.kiru.chat.application.service;

import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WebSocketUserService {
    private final SimpUserRegistry simpUserRegistry;
    private final ConcurrentMap<String, Boolean> userConnectionStatus = new ConcurrentHashMap<>();

    public boolean isUserConnected(String userId) {
        return userConnectionStatus.getOrDefault(userId, false);
    }

    public void updateUserConnectionStatus(String userId, boolean isConnected) {
        if (isConnected) {
            userConnectionStatus.put(userId, true);
        } else {
            userConnectionStatus.remove(userId);
        }
    }

    public List<Long> getConnectedUserIds() {
        return userConnectionStatus.entrySet().stream()
                .filter(Entry::getValue)
                .map(entry -> Long.parseLong(entry.getKey()))
                .toList();
    }
}