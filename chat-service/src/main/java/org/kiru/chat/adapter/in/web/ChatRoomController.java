package org.kiru.chat.adapter.in.web;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.kiru.chat.adapter.in.web.req.CreateChatRoomRequest;
import org.kiru.chat.adapter.in.web.res.AdminUserResponse;
import org.kiru.chat.adapter.in.web.res.CreateChatRoomResponse;
import org.kiru.chat.application.port.in.AddParticipantUseCase;
import org.kiru.chat.application.port.in.CreateRoomUseCase;
import org.kiru.chat.application.port.in.GetAlreadyLikedUserIdsUseCase;
import org.kiru.chat.application.port.in.GetChatRoomUseCase;
import org.kiru.chat.application.port.in.GetMessageUseCase;
import org.kiru.chat.application.port.in.SendMessageUseCase;
import org.kiru.chat.application.service.WebSocketUserService;
import org.kiru.chat.config.argumentresolve.UserId;
import org.kiru.core.chat.chatroom.domain.ChatRoom;
import org.kiru.core.chat.message.domain.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatRoomController {
    private final SendMessageUseCase sendMessageUseCase;
    private final CreateRoomUseCase createRoomUseCase;
    private final GetChatRoomUseCase getChatRoomUseCase;
    private final AddParticipantUseCase addParticipantUseCase;
    private final WebSocketUserService webSocketUserService;
    private final GetAlreadyLikedUserIdsUseCase getAlreadyUserIdsUseCase;
    private final GetMessageUseCase getMessageUseCase;

    @PostMapping("/rooms")
    public CreateChatRoomResponse createRoom(@UserId Long userId,
                                             @RequestBody CreateChatRoomRequest createChatRoomRequest) {
        return new CreateChatRoomResponse(createRoomUseCase.createRoom(createChatRoomRequest).getId());
    }

    @GetMapping("/rooms")
    public List<ChatRoom> getChatRoomsByUserId(@UserId Long userId) {
        return getChatRoomUseCase.findRoomsByUserId(userId);
    }

    @GetMapping("/rooms/{roomId}")
    public ChatRoom getRoom(@PathVariable Long roomId, @UserId Long userId,
                            @RequestParam(required = false, defaultValue = "false") Boolean changeStatus) {
        return getChatRoomUseCase.findRoomById(roomId, userId, changeStatus);
    }

    @MessageMapping("/chat.send/{roomId}")
    @SendTo("/topic/{roomId}")
    public Message sendMessage(@DestinationVariable Long roomId, @Payload Message message) {
        Long receiverId = message.getSendedId();
        message.setReadStatus(webSocketUserService.isUserConnected(receiverId.toString())); // 상대 유저가 접속 중이면 읽음 상태로 설정
        return sendMessageUseCase.sendMessage(roomId, message);
    }

    @PostMapping("/rooms/{roomId}/participants")
    public ResponseEntity<String> addParticipant(@PathVariable Long roomId, @UserId Long userId) {
        boolean added = addParticipantUseCase.addParticipant(roomId, userId);
        return added ? ResponseEntity.ok("Participant added")
                : ResponseEntity.badRequest().body("Failed to add participant");
    }

    @GetMapping("/me/rooms")
    public List<Long> getAlreadyLikedUserIds(@UserId Long userId) {
        return getAlreadyUserIdsUseCase.getAlreadyLikedUserIds(userId);
    }

    @GetMapping("/connect-user")
    public List<Long> getAllConnectedUser() {
        return webSocketUserService.getConnectedUserIds();
    }

    @GetMapping("/me/matched")
    public List<AdminUserResponse> getMatchedUsers(@UserId Long userId) {
        return getAlreadyUserIdsUseCase.getMatchedUsers(userId);
    }

    @GetMapping("/cs/rooms")
    public ChatRoom getMatchedUsers(@RequestParam Long adminId, @RequestParam Long userId) {
        return getChatRoomUseCase.getOrCreateRoomUseCase(userId, adminId);
    }

    @GetMapping("/rooms/{roomId}/messages")
    public Slice<Message> getMessageByRoomId(@PathVariable Long roomId, @UserId Long userId,
                                             @RequestParam Boolean admin, Pageable pageable) {
        return getMessageUseCase.getMessages(roomId, userId, admin, pageable);
    }
}