package org.kiru.chat.config.redis;

import com.netflix.appinfo.EurekaInstanceConfig;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kiru.chat.event.MessageEventListener;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.KeyspaceEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
@EnableScheduling
public class RedisStreamSubscriber {
    private final EurekaInstanceConfig eurekaInstanceConfig;
    private final MessageEventListener messageEventListener;
    private final RedisTemplate<String, String> redisTemplateForOne;
    private final RedisConnectionFactory redisConnectionFactoryForOne;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final RedisMessageListenerContainer redisMessageListenerContainer;

    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> messageListenerContainer;
    private String instanceId;

    @PostConstruct
    public void createConsumer() {
        instanceId = getInstanceId();
        log.info("Creating consumer for instanceId: {}", instanceId);
        createStreamConsumerGroup(instanceId, "messageConsumerGroup");
        removeKeyTTL(instanceId);
        initMessageListenerContainer();
        setupKeyExpirationListener();
    }

    private void setupKeyExpirationListener() {
        // Redis Keyspace Notifications 활성화
        redisTemplateForOne.execute((RedisCallback<Void>) connection -> {
            connection.execute("CONFIG", "SET".getBytes(), "notify-keyspace-events".getBytes(), "Ex".getBytes());
            return null;
        });

        // 키 만료 이벤트 리스너 등록
        redisMessageListenerContainer.addMessageListener(
                new KeyspaceEventMessageListener(redisMessageListenerContainer) {
                    @Override
                    protected void doHandleMessage(org.springframework.data.redis.connection.Message message) {
                        String key = message.toString();
                        if (key.equals(instanceId)) {
                            log.warn("Stream key has expired! Recreating stream: {}", key);
                            recreateStream(key);
                        }
                    }
                },
                new org.springframework.data.redis.listener.PatternTopic("__keyevent@*__:expired")
        );
    }

    private void recreateStream(String streamKey) {
        try {
            createStreamConsumerGroup(streamKey, "messageConsumerGroup");
            removeKeyTTL(streamKey);
            addHeartbeatToStream(streamKey);

            if (messageListenerContainer == null || !messageListenerContainer.isRunning()) {
                log.info("Listener container is not running, restarting it");
                restartMessageListenerContainer();
            }
        } catch (Exception e) {
            log.error("Failed to recreate stream: {}", e.getMessage(), e);
        }
    }

    private void initMessageListenerContainer() {
        messageListenerContainer = createStreamSubscription(
                instanceId, "messageConsumerGroup", "instance-1", messageEventListener
        );
    }

    private String getInstanceId() {
        return eurekaInstanceConfig.getInstanceId();
    }

    private void removeKeyTTL(String streamKey) {
        try {
            Boolean result = redisTemplateForOne.persist(streamKey);
            log.info("TTL removed from stream key {}: {}", streamKey, result);
        } catch (Exception e) {
            log.error("Failed to remove TTL from stream key: {}", e.getMessage(), e);
        }
    }

    private void addHeartbeatToStream(String streamKey) {
        try {
            redisTemplateForOne.opsForStream().add(
                    streamKey,
                    Collections.singletonMap("heartbeat", "ping")
            );
            log.info("Added heartbeat message to stream: {}", streamKey);
        } catch (Exception e) {
            log.error("Failed to add heartbeat to stream: {}", e.getMessage(), e);
        }
    }

    public void createStreamConsumerGroup(final String streamKey, final String consumerGroupName) {
        boolean streamExists = Boolean.TRUE.equals(redisTemplateForOne.hasKey(streamKey));
        if (!streamExists) {
            try {
                // 먼저 스트림에 초기 메시지 추가
                redisTemplateForOne.opsForStream().add(
                        streamKey,
                        Collections.singletonMap("init", "true")
                );
                log.info("Created stream with initial message: {}", streamKey);

                // 그런 다음 소비자 그룹 생성
                redisTemplateForOne.execute((RedisCallback<Void>) connection -> {
                    byte[] streamKeyBytes = streamKey.getBytes();
                    byte[] consumerGroupNameBytes = consumerGroupName.getBytes();
                    connection.execute("XGROUP", "CREATE".getBytes(), streamKeyBytes, consumerGroupNameBytes,
                            "0".getBytes(), "MKSTREAM".getBytes());
                    return null;
                });
                log.info("Created consumer group: {} for stream: {}", consumerGroupName, streamKey);
            } catch (Exception e) {
                log.error("Error creating stream with messages: {}", e.getMessage(), e);

                // 대체 방법으로 원래 방식 시도
                redisTemplateForOne.execute((RedisCallback<Void>) connection -> {
                    byte[] streamKeyBytes = streamKey.getBytes();
                    byte[] consumerGroupNameBytes = consumerGroupName.getBytes();
                    connection.execute("XGROUP", "CREATE".getBytes(), streamKeyBytes, consumerGroupNameBytes,
                            "0".getBytes(), "MKSTREAM".getBytes());
                    return null;
                });
            }
        } else if (!isStreamConsumerGroupExist(streamKey, consumerGroupName)) {
            redisTemplateForOne.opsForStream().createGroup(streamKey, ReadOffset.from("0"), consumerGroupName);
            log.info("Created consumer group for existing stream: {}", streamKey);
        }
    }

    public boolean isStreamConsumerGroupExist(final String streamKey, final String consumerGroupName) {
        return redisTemplateForOne
                .opsForStream().groups(streamKey).stream()
                .anyMatch(group -> group.groupName().equals(consumerGroupName));
    }

    @Bean
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer() {
        return createStreamSubscription(
                instanceId, "messageConsumerGroup", "instance-1", messageEventListener
        );
    }

    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> createStreamSubscription(
            String streamKey, String consumerGroup, String consumerName,
            StreamListener<String, MapRecord<String, String, String>> eventListener) {

        StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> containerOptions = StreamMessageListenerContainerOptions
                .builder()
                .pollTimeout(Duration.ofSeconds(1L))
                .errorHandler(e -> {
                    log.error("Error in listener: {}", e.getMessage(), e);
                    restartSubscription(streamKey, consumerGroup, consumerName, eventListener);
                }).build();

        StreamMessageListenerContainer<String, MapRecord<String, String, String>> container =
                StreamMessageListenerContainer.create(redisConnectionFactoryForOne, containerOptions);

        container.register(
                StreamMessageListenerContainer.StreamReadRequest.builder(
                                StreamOffset.create(streamKey, ReadOffset.lastConsumed()))
                        .cancelOnError(t -> true)
                        .consumer(Consumer.from(consumerGroup, consumerName))
                        .autoAcknowledge(true)
                        .build(), eventListener);

        container.start();
        log.info("Listener container started for stream: {}", streamKey);
        return container;
    }

    private void restartSubscription(String streamKey, String consumerGroup, String consumerName,
                                     StreamListener<String, MapRecord<String, String, String>> eventListener) {
        scheduler.schedule(() -> {
            log.info("Restarting subscription for stream: {}", streamKey);
            stopContainer(streamKey);

            boolean streamExists = Boolean.TRUE.equals(redisTemplateForOne.hasKey(streamKey));
            if (!streamExists) {
                log.warn("Stream key not found before restarting, recreating: {}", streamKey);
                createStreamConsumerGroup(streamKey, consumerGroup);
                removeKeyTTL(streamKey);
            }

            createStreamSubscription(streamKey, consumerGroup, consumerName, eventListener).start();
        }, 5, TimeUnit.SECONDS);
    }

    private void stopContainer(String streamKey) {
        if (instanceId.equals(streamKey) && messageListenerContainer != null && messageListenerContainer.isRunning()) {
            messageListenerContainer.stop();
            log.info("Stopped point listener container");
        }
    }

    private void restartMessageListenerContainer() {
        if (messageListenerContainer != null && messageListenerContainer.isRunning()) {
            messageListenerContainer.stop();
        }
        messageListenerContainer = createStreamSubscription(
                instanceId, "messageConsumerGroup", "instance-1", messageEventListener
        );
    }

    @PreDestroy
    public void onDestroy() {
        stopContainer(instanceId);
        scheduler.shutdown();
        if (redisMessageListenerContainer != null) {
            redisMessageListenerContainer.stop();
        }
        log.info("All listener containers stopped and schedulers shutdown.");
    }
}