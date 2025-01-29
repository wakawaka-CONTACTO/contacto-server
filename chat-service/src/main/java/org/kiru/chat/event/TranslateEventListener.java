package org.kiru.chat.event;

import static java.util.Objects.requireNonNull;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kiru.chat.adapter.in.web.req.OriginMessageDto;
import org.kiru.chat.application.port.in.MessageTranslateUseCase;
import org.kiru.chat.application.service.WebSocketUserService;
import org.kiru.core.chat.message.domain.TranslateLanguage;
import org.kiru.core.chat.message.domain.TranslateMessage;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
@Slf4j
public class TranslateEventListener {
    private final WebSocketUserService webSocketUserService;
    private final MessageTranslateUseCase translateUseCase;
    private final SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener
    public void handleMessageCreatedEvent(MessageCreateEvent event) {
        if (event.userId() == null) {
            log.warn("Translation event ignored: userId is null");
            return;
        }
        try {
            boolean isUserConnected = webSocketUserService.isUserConnected(event.userId());
            if (!isUserConnected) {
                log.info("User {} is not connected, skipping translation", event.userId());
                return;
            }
            TranslateLanguage translateLanguage = webSocketUserService.isUserConnectedAndTranslate(event.userId());
            if (translateLanguage == null) {
                log.info("No translation language set for user {}", event.userId());
                return;
            }
            handleTranslateEvent(event.userId(), List.of(event.messageId()));
        } catch (Exception e) {
            log.error("Failed to process translation event for user {}: {}", event.userId(), e.getMessage());
        }
    }

    @EventListener
    public void handleTranslateSubscribeEvent(UserTranslateSubscribeEvent event) {
        handleTranslateEvent(event.userId(), event.messageIds());
    }

    private void handleTranslateEvent(String userId, List<Long> messageIds) {
        TranslateLanguage translateLanguage = webSocketUserService.isUserConnectedAndTranslate(userId);
        requireNonNull(translateLanguage, "Translate language must be provided");
        requireNonNull(messageIds, "Message IDs must be provided");
        try {
            List<TranslateMessage> translateMessage = translateUseCase.translateMessage(
                    OriginMessageDto.of(messageIds, translateLanguage));
            messagingTemplate.convertAndSend(
                    "/queue/translate/" + userId,
                    translateMessage
            );
            log.info("Translation sent to user {} for messages {}", userId, messageIds);
        } catch (Exception e) {
            log.error("Translation failed for user {} and messages {}: {}",
                    userId, messageIds, e.getMessage());
        }
    }
}
