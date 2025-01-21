package org.kiru.chat.application.service;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.RequiredArgsConstructor;
import org.kiru.core.chat.message.domain.TranslateLanguage;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketUserService {
    private final ConcurrentMap<String, Boolean> userConnectionStatus = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, TranslateLanguage> userTranslateStatus = new ConcurrentHashMap<>();

    public boolean isUserConnected(String userId) {
        return userConnectionStatus.getOrDefault(userId, false);
    }

    // 사용자 연결 상태 업데이트
    public void updateUserConnectionStatus(final String userId, final boolean isConnected) {
        if (isConnected) {
            userConnectionStatus.put(userId, true);
        }else{
            userConnectionStatus.remove(userId);
            userTranslateStatus.remove(userId);
        }
    }

    public List<Long> getConnectedUserIds() {
        return userConnectionStatus.entrySet().stream()
                .filter(Entry::getValue)
                .map(entry -> Long.parseLong(entry.getKey()))
                .toList();
    }

    public void updateUserTranslationPreference(final String userId,final String targetLanguage) {
        userTranslateStatus.put(userId, TranslateLanguage.valueOf(targetLanguage));
    }

    public TranslateLanguage isUserConnectedAndTranslate(final String userId) {
        requireNonNull(userId, "User ID must be provided");
        if(Boolean.TRUE.equals(userConnectionStatus.getOrDefault(userId, false)) && userTranslateStatus.containsKey(userId)){
            return userTranslateStatus.get(userId);
        }
        return null;
    }
}