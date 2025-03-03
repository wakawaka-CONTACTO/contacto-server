package org.kiru.chat.config.redis;


import com.netflix.appinfo.EurekaInstanceConfig;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

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

    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> messageListenerContainer;

    @PostConstruct
    public void createConsumer() {
        String instanceId = getInstanceId(); // Eureka의 고유 ID 가져오기
        createStreamConsumerGroup(instanceId, "messageConsumerGroup");
    }

    private String getInstanceId() {
        return eurekaInstanceConfig.getInstanceId();
    }

    public void createStreamConsumerGroup(final String streamKey, final String consumerGroupName) {
        boolean streamExists = Boolean.TRUE.equals(redisTemplateForOne.hasKey(streamKey));
        if (!streamExists) {
            redisTemplateForOne.execute((RedisCallback<Void>) connection -> {
                byte[] streamKeyBytes = streamKey.getBytes();
                byte[] consumerGroupNameBytes = consumerGroupName.getBytes();
                connection.execute("XGROUP", "CREATE".getBytes(), streamKeyBytes, consumerGroupNameBytes,
                        "0".getBytes(), "MKSTREAM".getBytes());
                return null;
            });
        } else if (!isStreamConsumerGroupExist(streamKey, consumerGroupName)) {
            redisTemplateForOne.opsForStream().createGroup(streamKey, ReadOffset.from("0"), consumerGroupName);
        }
    }

    public boolean isStreamConsumerGroupExist(final String streamKey, final String consumerGroupName) {
        return redisTemplateForOne
                .opsForStream().groups(streamKey).stream()
                .anyMatch(group -> group.groupName().equals(consumerGroupName));
    }

    @Bean
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> startMessageListener() {
        messageListenerContainer = createStreamSubscription(
                getInstanceId(), "messageConsumerGroup", "instance-1", messageEventListener
        );
        return messageListenerContainer;
    }

    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> createStreamSubscription(
            String streamKey, String consumerGroup, String consumerName,
            StreamListener<String, MapRecord<String, String, String>> eventListener) {

        StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> containerOptions = StreamMessageListenerContainerOptions
                .builder()
                .pollTimeout(Duration.ofSeconds(1L))
                .errorHandler(e -> {
                    log.error("Error in listener: {}", e.getMessage());
                    restartSubscription(streamKey, consumerGroup, consumerName, eventListener);
                }).build();

        StreamMessageListenerContainer<String, MapRecord<String, String, String>> container =
                StreamMessageListenerContainer.create(redisConnectionFactoryForOne, containerOptions);

        container.register(
                StreamMessageListenerContainer.StreamReadRequest.builder(
                                StreamOffset.create(streamKey, ReadOffset.lastConsumed()))
                        .cancelOnError(t -> true) // 오류 발생 시 구독 취소
                        .consumer(Consumer.from(consumerGroup, consumerName))
                        .autoAcknowledge(false)
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
            createStreamSubscription(streamKey, consumerGroup, consumerName, eventListener).start();
        }, 5, TimeUnit.SECONDS); // 일정 시간 후 재시작
    }

    private void stopContainer(String streamKey) {
        if (getInstanceId().equals(streamKey) && messageListenerContainer != null && messageListenerContainer.isRunning()) {
            messageListenerContainer.stop();
            log.info("Stopped point listener container");
        }
    }

    @PreDestroy
    public void onDestroy() {
        stopContainer(getInstanceId());
        scheduler.shutdown();
        log.info("All listener containers stopped and scheduler shutdown.");
    }
}