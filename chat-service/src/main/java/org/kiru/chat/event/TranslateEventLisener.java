package org.kiru.chat.event;

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
public class TranslateEventLisener {
    private final WebSocketUserService webSocketUserService;
    private final MessageTranslateUseCase translateUseCase;
    private final SimpMessagingTemplate messagingTemplate;  // 추가

    @TransactionalEventListener
    public void handleMessageCreatedEvent(MessageCreateEvent event) {
        TranslateLanguage translateLanguage = webSocketUserService.isUserConnectedAndTranslate(event.userId().toString());
        List<TranslateMessage> translateMessage = translateUseCase.translateMessage(OriginMessageDto.of(event.messageId(), translateLanguage));
        messagingTemplate.convertAndSend(
                "/queue/translate/"+event.userId(),         // 구독 엔드포인트
                translateMessage           // 전송할 메시지
        );
        log.info(messagingTemplate.getMessageChannel().toString());
    }

    @EventListener
    public void handleTranslateSubscribeEvent(UserTranslateSubscribeEvent event) {
        TranslateLanguage translateLanguage = webSocketUserService.isUserConnectedAndTranslate(event.userId());
        List<TranslateMessage> translateMessage = translateUseCase.translateMessage(OriginMessageDto.of(event.messageIds(), translateLanguage));
        messagingTemplate.convertAndSend(
                "/queue/translate/"+event.userId(),         // 구독 엔드포인트
                translateMessage           // 전송할 메시지
        );
        log.info(messagingTemplate.getMessageChannel().toString());
    }
}
