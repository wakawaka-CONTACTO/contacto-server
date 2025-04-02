package org.kiru.chat.adapter.in.web;

import static java.util.Optional.*;

import org.kiru.chat.application.port.in.SaveMessageUseCase;
import org.kiru.chat.application.port.in.SendMessageUseCase;
import org.kiru.chat.application.service.ChatNotificationService;
import org.kiru.chat.application.service.WebSocketUserService;
import org.kiru.core.chat.message.domain.Message;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
@RestController
@RequestMapping("/api/v1/chat")
public class WebSocketHandler {

    private final SendMessageUseCase sendMessageUseCase;
    private final SaveMessageUseCase saveMessageUseCase;
    private final WebSocketUserService webSocketUserService;
    private final ChatNotificationService chatNotificationService;

    @MessageMapping("/chat.send/{roomId}")
    public Message sendMessage(@DestinationVariable @Validated Long roomId, @Validated @Payload Message message) {
        // 유효성 검사
        ofNullable(message).orElseThrow(() -> new IllegalArgumentException("Message must be provided"));
        ofNullable(message.getSendedId()).orElseThrow(() -> new IllegalArgumentException("Receiver(Sended) ID must be provided"));
        
        // 채팅방 설정
        Long receiverId = message.getSendedId();
        Long senderId = message.getSenderId();
        message.chatRoom(roomId);
        
        // 기본적으로 메시지는 읽지 않음 상태로 설정
        message.setReadStatus(false);
        
        // 수신자가 같은 채팅방에 접속 중인지 확인
        boolean isReceiverInRoom = webSocketUserService.isUserInChatRoom(receiverId.toString(), roomId);
        
        // 수신자가 같은 채팅방에 있으면 읽음 처리
        if (isReceiverInRoom) {
            message.toRead();
        }
        
        // 수신자가 접속 중인지 확인
        boolean isReceiverConnected = webSocketUserService.isUserConnected(receiverId.toString());
        
        // 메시지 처리 및 알림
        if (isReceiverConnected) {
            // 수신자가 접속 중이면 메시지 전송
            return sendMessageUseCase.sendMessage(roomId, message);
        } else {
            // 수신자가 접속 중이 아니면 메시지 저장 후 푸시 알림
            Message savedMessage = saveMessageUseCase.saveMessage(roomId, message);
            chatNotificationService.sendNotification(message);
            return savedMessage;
        }
    }
}
