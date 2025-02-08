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
    private static final String CONNECT_ID = "userId";
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String userId = (String) requireNonNull(headerAccessor.getSessionAttributes()).get(CONNECT_ID);
        //1. 사용자 Local 연결 상태 업데이트
        webSocketUserService.updateUserConnectionStatus(userId, true);
        log.info("User connected >>>: {}", userId);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String userId = (String) requireNonNull(headerAccessor.getSessionAttributes()).get(CONNECT_ID);
        webSocketUserService.updateUserConnectionStatus(userId, false);
        log.info("User disconnected >>> : {}", userId);
    }

    @EventListener
    public void handleSubscribeEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = headerAccessor.getDestination();
        // 번역 구독인 경우에만 처리
        if (destination != null && destination.contains(TRANSLATION_QUEUE_PREFIX)) {
            String targetLanguage = requireNonNull(headerAccessor.getFirstNativeHeader("targetLanguage"),
                    "Target language must be provided");
            String userId = requireNonNull((String) headerAccessor.getSessionAttributes().get(CONNECT_ID),
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
