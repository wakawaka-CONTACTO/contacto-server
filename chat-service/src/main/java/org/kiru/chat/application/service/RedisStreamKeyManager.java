package org.kiru.chat.application.service;

import com.netflix.appinfo.EurekaInstanceConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisStreamKeyManager {
    private final RedisTemplate<String, String> redisTemplateForOne;
    private final EurekaInstanceConfig eurekaInstanceConfig;
    private final Set<String> managedStreamKeys = ConcurrentHashMap.newKeySet();
    private String instanceId;

    @PostConstruct
    public void init() {
        instanceId = eurekaInstanceConfig.getInstanceId();
        managedStreamKeys.add(instanceId);
    }

    public void addStreamKey(String streamKey) {
        managedStreamKeys.add(streamKey);
        ensureStreamKeyPersistence(streamKey);
    }

    @Scheduled(fixedRate = 30000) // 30초마다 실행
    public void checkAndRestoreStreamKeys() {
        log.info("Checking stream keys persistence...");
        managedStreamKeys.forEach(this::ensureStreamKeyPersistence);
    }

    private void ensureStreamKeyPersistence(String streamKey) {
        try {
            // 키의 TTL 확인
            Long ttl = redisTemplateForOne.getExpire(streamKey);

            if (ttl == -2) {
                // 키가 존재하지 않거나 만료된 경우
                log.warn("Stream key {} not found or expired (TTL: {}), attempting to restore", streamKey, ttl);

                // 스트림 키 재생성
                redisTemplateForOne.execute((RedisCallback<Void>) connection -> {
                    connection.multi();
                    try {
                        connection.execute("XGROUP", "CREATE".getBytes(),
                                streamKey.getBytes(),
                                "messageConsumerGroup".getBytes(),
                                "0".getBytes(),
                                "MKSTREAM".getBytes());
                    } catch (Exception ex) {
                        if (ex.getMessage() != null && ex.getMessage().contains("BUSYGROUP")) {
                            log.warn("Stream key {} is busy, attempting to set ID", streamKey);
                            // 필요하다면 XGROUP SETID 로직 처리
                        } else {
                            throw ex;
                        }
                    }
                    connection.exec();
                    return null;
                });

                // TTL을 -1(영구)로 설정
                redisTemplateForOne.persist(streamKey);
                log.info("Successfully restored stream key: {} and set TTL to -1", streamKey);
            } else if (ttl != -1) {
                // TTL이 설정되어 있지만 영구가 아닌 경우
                log.info("Stream key {} has TTL: {}, setting to persistent", streamKey, ttl);
                redisTemplateForOne.persist(streamKey);
            }
        } catch (Exception e) {
            log.error("Error ensuring stream key persistence for {}: {}", streamKey, e.getMessage(), e);
        }
    }
} 