package org.kiru.chat.application.service;

import static java.util.Objects.requireNonNull;

import com.netflix.appinfo.EurekaInstanceConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.kiru.chat.application.port.out.SendMessagePort;
import org.kiru.core.chat.message.domain.Message;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisService implements SendMessagePort {
    private final RedisTemplate<String, String> redisTemplateForOne;
    private final EurekaInstanceConfig eurekaInstanceConfig;
    private String INSTANCE_ID;

    @PostConstruct
    public final void getServerKey() {
        INSTANCE_ID = eurekaInstanceConfig.getInstanceId();
    }

    @Override
    public void sendMessage(Message message, String receiverId) {
        String serverKey = redisTemplateForOne.opsForValue().get(receiverId);
        requireNonNull(serverKey, "Server key must be provided");
        redisTemplateForOne.opsForStream().add(serverKey, message.toMap(receiverId));
    }

    @Override
    public void sendMessageAck(String recordId) {
        redisTemplateForOne.opsForStream().acknowledge(INSTANCE_ID, "messageConsumerGroup", recordId);
    }
}
