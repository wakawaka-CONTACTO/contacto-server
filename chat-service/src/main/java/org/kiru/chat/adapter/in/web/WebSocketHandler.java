package org.kiru.chat.adapter.in.web;

import static java.util.Optional.ofNullable;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kiru.chat.application.port.in.SaveMessageUseCase;
import org.kiru.chat.application.port.in.SendMessageUseCase;
import org.kiru.chat.application.service.WebSocketUserService;
import org.kiru.core.chat.message.domain.Message;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
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
    private final SaveMessageUseCase saveMessageUseCase;
    private final WebSocketUserService webSocketUserService;

    @MessageMapping("/chat.send/{roomId}")
//    @SendTo({"/topic/{roomId}"})+
    public Message sendMessage(@DestinationVariable @Validated Long roomId, @Validated @Payload Message message) {
        ofNullable(message).orElseThrow(() -> new IllegalArgumentException("Message must be provided"));
        ofNullable(message.getSendedId()).orElseThrow(() -> new IllegalArgumentException("Receiver(Sended) ID must be provided"));
        Long receiverId = message.getSendedId();
        message.chatRoom(roomId);
        // 1. 상대방이 접속 중인지 확인
        boolean isUserConnected = webSocketUserService.isUserConnected(receiverId.toString());
        // 2. 상대방이 접속중이 아니면 읽지 않음 상태로 메시지 저장
        if(!isUserConnected) {
            message.toRead();
            return saveMessageUseCase.saveMessage(roomId, message);
        }
        return  sendMessageUseCase.sendMessage(roomId, message);
    }
}
