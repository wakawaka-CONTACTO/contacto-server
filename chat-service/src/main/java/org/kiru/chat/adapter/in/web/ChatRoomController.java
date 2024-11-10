package org.kiru.chat.adapter.in.web;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.kiru.chat.application.port.in.AddParticipantUseCase;
import org.kiru.chat.application.port.in.CreateRoomUseCase;
import org.kiru.chat.application.port.in.GetChatRoomUseCase;
import org.kiru.chat.application.port.in.SendMessageUseCase;
import org.kiru.chat.config.argumentresolve.UserId;
import org.kiru.core.chat.chatroom.domain.ChatRoom;
import org.kiru.core.chat.message.domain.Message;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatRoomController {
    private final SendMessageUseCase sendMessageUseCase;
    private final CreateRoomUseCase createRoomUseCase;
    private final GetChatRoomUseCase getChatRoomUseCase;
    private final AddParticipantUseCase addParticipantUseCase;

    @PostMapping("/rooms")
    public CreateChatRoomResponse createRoom(@UserId Long userId,@RequestBody CreateChatRoomRequest createChatRoomRequest) {
            return new CreateChatRoomResponse(createRoomUseCase.createRoom(createChatRoomRequest).getId());
    }

    @GetMapping("/rooms")
    public List<ChatRoom> getChatRoomsByUserId(@UserId Long userId) {
        return getChatRoomUseCase.findRoomsByUserId(userId);
    }

    @GetMapping("/rooms/{roomId}")
    public ChatRoom getRoom(@PathVariable Long roomId, @UserId Long userId) {
        return getChatRoomUseCase.findRoomById(roomId,userId);
    }

    @MessageMapping("/chat.send/{roomId}")
    @SendTo("/topic/{roomId}")
    public Message sendMessage(@DestinationVariable Long roomId, @Payload Message message) {
        return sendMessageUseCase.sendMessage(roomId, message);
    }

    @PostMapping("/rooms/{roomId}/participants")
    public ResponseEntity<String> addParticipant(@PathVariable Long roomId, @UserId Long userId) {
        boolean added = addParticipantUseCase.addParticipant(roomId, userId);
        return added ? ResponseEntity.ok("Participant added") : ResponseEntity.badRequest().body("Failed to add participant");
    }
}