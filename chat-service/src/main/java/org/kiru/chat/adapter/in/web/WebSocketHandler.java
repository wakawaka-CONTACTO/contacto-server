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
        ofNullable(message).orElseThrow(() -> new IllegalArgumentException("Message must be provided"));
        ofNullable(message.getSendedId()).orElseThrow(() -> new IllegalArgumentException("Receiver(Sended) ID must be provided"));
        
        Long receiverId = message.getSendedId();
        Long senderId = message.getSenderId();
        message.chatRoom(roomId);
        
<<<<<<< Updated upstream
        // 1. 상대방이 접속 중인지 확인
        boolean isUserConnected = webSocketUserService.isUserConnected(receiverId.toString());
        
        // 2. 상대방이 접속중이면 읽음 처리 후 메시지 전송
        if(isUserConnected) {
            message.toRead();
            return sendMessageUseCase.sendMessage(roomId, message);
        }
        
        // 3. 메시지 저장
        Message savedMessage = saveMessageUseCase.saveMessage(roomId, message);
        
        // 4. 푸시 알림 전송
        chatNotificationService.sendNotification(message);
        
        return savedMessage;
=======
        // 자신에게 보낸 메시지이거나 자신이 보낸 메시지는 읽음 처리
        if (senderId != null && senderId.equals(receiverId)) {
            message.toRead();
        }
        
        // 상대방 연결 여부 확인 (읽음 처리와는 무관, 단지 메시지 전송 방식 결정을 위함)
        boolean isUserConnected = webSocketUserService.isUserConnected(receiverId.toString());
        if (isUserConnected) {
            return sendMessageUseCase.sendMessage(roomId, message);
        } else {
            return saveMessageUseCase.saveMessage(roomId, message);
        }
>>>>>>> Stashed changes
    }
}
