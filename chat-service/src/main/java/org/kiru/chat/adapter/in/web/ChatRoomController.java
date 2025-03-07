package org.kiru.chat.adapter.in.web;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.kiru.chat.adapter.in.web.req.CreateChatRoomRequest;
import org.kiru.chat.adapter.in.web.res.CreateChatRoomResponse;
import org.kiru.core.common.PageableResponse;
import org.kiru.chat.application.port.in.CreateRoomUseCase;
import org.kiru.chat.application.port.in.GetChatRoomUseCase;
import org.kiru.chat.application.port.in.GetMessageUseCase;
import org.kiru.chat.application.service.WebSocketUserService;
import org.kiru.chat.config.argumentresolve.UserId;
import org.kiru.core.chat.chatroom.domain.ChatRoom;
import org.kiru.core.chat.message.domain.Message;
import org.springframework.data.domain.Pageable;
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
    private final CreateRoomUseCase createRoomUseCase;
    private final GetChatRoomUseCase getChatRoomUseCase;
    private final WebSocketUserService webSocketUserService;
    private final GetMessageUseCase getMessageUseCase;

    @PostMapping("/rooms")
    public CreateChatRoomResponse createRoom(@UserId Long userId,
                                             @RequestBody CreateChatRoomRequest createChatRoomRequest) {
        return new CreateChatRoomResponse(createRoomUseCase.createRoom(createChatRoomRequest).getId());
    }

    @GetMapping("/rooms")
    public PageableResponse<ChatRoom> getChatRoomsByUserId(@UserId Long userId, Pageable pageable) {
        return getChatRoomUseCase.findRoomsByUserId(userId,pageable);
    }

    @GetMapping("/rooms/{roomId}")
    public ChatRoom getRoom(@PathVariable("roomId") Long roomId, @UserId Long userId,
                            @RequestParam(required = false, defaultValue = "false") boolean changeStatus) {
        return getChatRoomUseCase.findRoomById(roomId, userId, changeStatus);
    }

    @GetMapping("/connect-user")
    public List<Long> getAllConnectedUser() {
        return webSocketUserService.getConnectedUserIds();
    }

    @GetMapping("/cs/rooms")
    public ChatRoom getMatchedUsers(@RequestParam Long adminId, @RequestParam Long userId) {
        return getChatRoomUseCase.getOrCreateRoomUseCase(userId, adminId);
    }

    @GetMapping("/rooms/{roomId}/messages")
    public List<Message> getMessageByRoomId(@PathVariable Long roomId, @UserId Long userId,
                                             @RequestParam Boolean admin, Pageable pageable) {
        return getMessageUseCase.getMessages(roomId, userId, admin, pageable);
    }
}