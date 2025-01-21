package org.kiru.chat.adapter.in.web;

import static java.util.Optional.ofNullable;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kiru.chat.application.port.in.SendMessageUseCase;
import org.kiru.chat.application.service.WebSocketUserService;
import org.kiru.core.chat.message.domain.Message;
import org.kiru.core.chat.message.domain.TranslateLanguage;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@Component
@RestController
@RequestMapping("/api/v1/chat")
public class WebSocketHandler {

    private final SendMessageUseCase sendMessageUseCase;
    private final WebSocketUserService webSocketUserService;

    @MessageMapping("/chat.send/{roomId}")
    @SendTo("/topic/{roomId}")
    public Message sendMessage(@DestinationVariable @Validated Long roomId, @Validated @Payload Message message) {
        ofNullable(message).orElseThrow(() -> new IllegalArgumentException("Message must be provided"));
        ofNullable(message.getSendedId()).orElseThrow(() -> new IllegalArgumentException("Receiver(Sended) ID must be provided"));
        Long receiverId = message.getSendedId();
        boolean isUserConnected = webSocketUserService.isUserConnected(receiverId.toString());
        TranslateLanguage translateLanguage = webSocketUserService.isUserConnectedAndTranslate(receiverId.toString());
        message.setReadStatus(isUserConnected); // 상대 유저가 접속 중이면 읽음 상태로 설정
        return  sendMessageUseCase.sendMessage(roomId, message,isUserConnected,translateLanguage);
    }
}
