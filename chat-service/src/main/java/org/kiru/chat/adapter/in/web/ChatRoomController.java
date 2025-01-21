package org.kiru.chat.adapter.in.web;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.kiru.chat.adapter.in.web.req.CreateChatRoomRequest;
import org.kiru.chat.adapter.in.web.res.CreateChatRoomResponse;
import org.kiru.chat.application.port.in.CreateRoomUseCase;
import org.kiru.chat.application.port.in.GetChatRoomUseCase;
import org.kiru.chat.application.port.in.GetMessageUseCase;
import org.kiru.chat.application.service.WebSocketUserService;
import org.kiru.chat.config.argumentresolve.UserId;
import org.kiru.core.chat.chatroom.domain.ChatRoom;
import org.kiru.core.chat.message.domain.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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

    /**
     * Creates a new chat room for a specific user.
     *
     * @param userId The ID of the user creating the chat room
     * @param createChatRoomRequest The request containing details for creating the chat room
     * @return A response containing the ID of the newly created chat room
     */
    @PostMapping("/rooms")
    public CreateChatRoomResponse createRoom(@UserId Long userId,
                                             @RequestBody CreateChatRoomRequest createChatRoomRequest) {
        return new CreateChatRoomResponse(createRoomUseCase.createRoom(createChatRoomRequest).getId());
    }

    @GetMapping("/rooms")
    public List<ChatRoom> getChatRoomsByUserId(@UserId Long userId, Pageable pageable) {
        return getChatRoomUseCase.findRoomsByUserId(userId,pageable);
    }

    /**
     * Retrieves a specific chat room by its ID, with optional status change.
     *
     * @param roomId The unique identifier of the chat room to retrieve
     * @param userId The ID of the user requesting the chat room
     * @param changeStatus Flag to indicate whether the room's status should be modified (default is false)
     * @return The requested ChatRoom with potential status updates
     */
    @GetMapping("/rooms/{roomId}")
    public ChatRoom getRoom(@PathVariable("roomId") Long roomId, @UserId Long userId,
                            @RequestParam(required = false, defaultValue = "false") boolean changeStatus) {
        return getChatRoomUseCase.findRoomById(roomId, userId, changeStatus);
    }

    /**
     * Retrieves a list of all currently connected user IDs via WebSocket.
     *
     * @return A list of user IDs that are currently connected to the WebSocket service
     */
    @GetMapping("/connect-user")
    public List<Long> getAllConnectedUser() {
        return webSocketUserService.getConnectedUserIds();
    }

    /**
     * Retrieves or creates a chat room for a specific admin and user.
     *
     * @param adminId The ID of the admin user
     * @param userId The ID of the user
     * @return The existing or newly created ChatRoom between the specified admin and user
     */
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