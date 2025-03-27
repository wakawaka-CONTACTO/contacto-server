package org.kiru.chat.application.service;

import static java.util.Objects.requireNonNull;

import com.netflix.appinfo.EurekaInstanceConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kiru.chat.application.port.out.SendMessagePort;
import org.kiru.core.chat.message.domain.Message;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService implements SendMessagePort {
    private final RedisTemplate<String, String> redisTemplateForOne;
    private final EurekaInstanceConfig eurekaInstanceConfig;
    private final RedisStreamKeyManager streamKeyManager;
    private String INSTANCE_ID;

    @PostConstruct
    public final void getServerKey() {
        INSTANCE_ID = eurekaInstanceConfig.getInstanceId();
    }

    @Override
    public void sendMessage(Message message, String receiverId) {
        try {
            String serverKey = redisTemplateForOne.opsForValue().get(receiverId);
            requireNonNull(serverKey, "Server key must be provided");
            
            // 스트림 키 관리에 추가
            streamKeyManager.addStreamKey(serverKey);
            
            // 메시지 전송
            redisTemplateForOne.opsForStream().add(serverKey, message.toMap(receiverId));
            log.debug("Message sent successfully to stream: {}", serverKey);
        } catch (Exception e) {
            log.error("Failed to send message to Redis stream: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void sendMessageAck(String recordId) {
        try {
            redisTemplateForOne.opsForStream().acknowledge(INSTANCE_ID, "messageConsumerGroup", recordId);
            log.debug("Message acknowledged successfully: {}", recordId);
        } catch (Exception e) {
            log.error("Failed to acknowledge message: {}", e.getMessage(), e);
            throw e;
        }
    }
}
