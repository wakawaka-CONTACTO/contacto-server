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
import lombok.extern.slf4j.Slf4j;
import org.kiru.core.chat.message.domain.TranslateLanguage;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
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
            try{
                Long parseLong = Long.parseLong(s);
                list.add(parseLong);
            }catch (NumberFormatException e){
                log.error("Failed to parse user id from redis key: {}", s);
            }
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
    
    // 사용자가 특정 채팅방에 접속 중인지 확인하는 메서드
    // 현재는 단순히 접속 여부만 확인하지만, 추후 특정 채팅방 구독 상태까지 확인하도록 확장 가능
    public boolean isUserInChatRoom(final String userId, final Long chatRoomId) {
        // 일단 사용자가 접속 중인지만 확인
        return isUserConnected(userId);
        
        // 추후 개선: Redis에 사용자별 현재 활성화된 채팅방 정보를 저장하여 확인
        // String activeRoomKey = "user:" + userId + ":active_room";
        // String activeRoomId = redisTemplateForOne.opsForValue().get(activeRoomKey);
        // return chatRoomId.toString().equals(activeRoomId);
    }
}