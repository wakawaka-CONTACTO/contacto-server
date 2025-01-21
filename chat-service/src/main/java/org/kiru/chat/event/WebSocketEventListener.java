package org.kiru.chat.event;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kiru.chat.application.service.WebSocketUserService;
import org.kiru.core.chat.message.domain.TranslateLanguage;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {
    private final WebSocketUserService webSocketUserService;
    private final ApplicationEventPublisher eventPublisher;
    private static final String TRANSLATION_QUEUE_PREFIX = "/queue/translate";

    /**
     * Handles WebSocket connection events and updates the user's connection status.
     *
     * @param event The WebSocket session connect event containing connection details
     * @throws NullPointerException if the user ID cannot be extracted from session attributes
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        webSocketUserService.updateUserConnectionStatus(userId, true);
        log.info("User connected >>>: {}", userId);
    }

    /**
     * Handles WebSocket disconnection events for a user.
     *
     * @param event The {@link SessionDisconnectEvent} representing the WebSocket disconnection
     * @throws NullPointerException if the user ID cannot be retrieved from session attributes
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        webSocketUserService.updateUserConnectionStatus(userId, false);
        log.info("User disconnected >>> : {}", userId);
    }

    /**
     * Handles WebSocket subscription events for translation services.
     *
     * This method processes subscription events specifically for translation queues. It validates
     * and extracts necessary information such as target language, user ID, and message IDs from
     * the WebSocket session headers.
     *
     * @param event The WebSocket session subscribe event to be processed
     * @throws NullPointerException if target language, user ID, or message IDs are not provided
     *
     * When a subscription to a translation queue is detected, this method:
     * 1. Validates the presence of required headers
     * 2. Converts comma-separated message IDs to a list of Long
     * 3. Updates the user's translation language preference
     * 4. Publishes a {@link UserTranslateSubscribeEvent}
     * 5. Logs the subscription details
     *
     * @see WebSocketUserService
     * @see UserTranslateSubscribeEvent
     */
    @EventListener
    public void handleSubscribeEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = headerAccessor.getDestination();
        // 번역 구독인 경우에만 처리
        if (destination != null && destination.contains(TRANSLATION_QUEUE_PREFIX)) {
            String targetLanguage = requireNonNull(headerAccessor.getFirstNativeHeader("targetLanguage"),
                    "Target language must be provided");
            String userId = requireNonNull((String) headerAccessor.getSessionAttributes().get("userId"),
                    "User ID must be provided");
            String messageIds = requireNonNull(headerAccessor.getFirstNativeHeader("messageIds")
                    , "Message IDs must be provided");
            if (!messageIds.isEmpty()) {
                // 쉼표로 구분된 메시지 ID 문자열을 리스트로 변환
                List<Long> messageIdList = Arrays.stream(messageIds.split(","))
                        .map(Long::parseLong)
                        .toList();
                // 사용자의 번역 언어 설정을 업데이트
                webSocketUserService.updateUserTranslationPreference(userId, targetLanguage);
                eventPublisher.publishEvent(
                        new UserTranslateSubscribeEvent(userId, TranslateLanguage.valueOf(targetLanguage),
                                messageIdList));
                log.info("User subscribed to translation service | userId={} | language={} | messageCount={}",
                        userId, targetLanguage, messageIdList.size());
            }
        }
    }
}
