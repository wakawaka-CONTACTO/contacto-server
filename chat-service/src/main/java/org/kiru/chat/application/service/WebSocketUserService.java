package org.kiru.chat.application.service;

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
    public void updateUserConnectionStatus(String userId, boolean isConnected) {
        if (isConnected) {
            userConnectionStatus.put(userId, true);
        }
    }

    public List<Long> getConnectedUserIds() {
        return userConnectionStatus.entrySet().stream()
                .filter(Entry::getValue)
                .map(entry -> Long.parseLong(entry.getKey()))
                .toList();
    }

    public void updateUserTranslationPreference(String userId, String targetLanguage) {
        userTranslateStatus.put(userId, TranslateLanguage.valueOf(targetLanguage));
    }

    public TranslateLanguage isUserConnectedAndTranslate(String userId) {
        if(userConnectionStatus.getOrDefault(userId, false) && userTranslateStatus.containsKey(userId)){
            return userTranslateStatus.get(userId);
        }
        return null;
    }
}