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

    /**
     * Handles the event triggered when a new message is created, initiating the translation process.
     *
     * @param event The message creation event containing the user ID and message ID
     * @see MessageCreateEvent
     */
    @TransactionalEventListener
    public void handleMessageCreatedEvent(MessageCreateEvent event) {
        handleTranslateEvent(event.userId(), List.of(event.messageId()));
    }

    /**
     * Handles a user's request to subscribe to message translations.
     *
     * @param event The event containing the user ID and list of message IDs to be translated
     * @see UserTranslateSubscribeEvent
     */
    @EventListener
    public void handleTranslateSubscribeEvent(UserTranslateSubscribeEvent event) {
        handleTranslateEvent(event.userId(), event.messageIds());
    }

    /**
     * Handles the translation of messages for a specific user.
     *
     * @param userId The unique identifier of the user requesting translation
     * @param messageIds A list of message IDs to be translated
     *
     * @throws NullPointerException if translation language or message IDs are null
     *
     * Attempts to translate messages for a connected user and send the translated
     * messages via WebSocket. If translation fails, an error is logged.
     */
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
