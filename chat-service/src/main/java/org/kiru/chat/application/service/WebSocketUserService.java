package org.kiru.chat.application.service;

import static java.util.Objects.requireNonNull;

import com.netflix.appinfo.EurekaInstanceConfig;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.RequiredArgsConstructor;
import org.kiru.core.chat.message.domain.TranslateLanguage;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketUserService {
    private final ConcurrentMap<String, Boolean> userConnectionStatus = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, TranslateLanguage> userTranslateStatus = new ConcurrentHashMap<>();
    private final RedisTemplate<String, String> redisTemplateForOne;
    private final EurekaInstanceConfig eurekaInstanceConfig;
    private String INSTANCE_ID;

    @PostConstruct
    public final void getServerKey() {
          INSTANCE_ID =eurekaInstanceConfig.getInstanceId();
    }

    public boolean isUserConnected(String userId) {
        return redisTemplateForOne.opsForValue().get(userId) != null;
    }
    // 사용자 연결 상태 업데이트
    public void updateUserConnectionStatus(final String userId, final boolean isConnected) {
        if (isConnected) {
            userConnectionStatus.put(userId, true);
            redisTemplateForOne.opsForValue().set(userId,INSTANCE_ID);
        }else{
            userConnectionStatus.remove(userId);
            userTranslateStatus.remove(userId);
            redisTemplateForOne.delete(userId);
        }
    }

    public List<Long> getConnectedUserIds() {
        List<Long> list = new ArrayList<>();
        for (String s : Optional.of(redisTemplateForOne.keys("*")).orElse(Set.of())) {
            Long parseLong = Long.parseLong(s);
            list.add(parseLong);
        }
        return list;
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