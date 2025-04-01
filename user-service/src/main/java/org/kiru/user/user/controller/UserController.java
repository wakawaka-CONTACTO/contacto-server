package org.kiru.user.user.controller;

import lombok.RequiredArgsConstructor;
import org.kiru.core.chat.chatroom.domain.ChatRoom;
import org.kiru.core.common.PageableResponse;
import org.kiru.core.user.user.domain.User;
import org.kiru.user.auth.argumentresolve.UserId;
import org.kiru.user.user.dto.response.ChatRoomListResponse;
import org.kiru.user.user.dto.response.ChatRoomResponse;
import org.kiru.user.user.dto.response.MessageResponse;
import org.kiru.user.user.dto.response.UserWithAdditionalInfoResponse;
import org.kiru.user.user.service.UserService;
import org.springframework.boot.actuate.autoconfigure.tracing.ConditionalOnEnabledTracing;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@ConditionalOnEnabledTracing
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserWithAdditionalInfoResponse> getUser(@UserId Long userId){
        User user = userService.getUserFromIdToMainPage(userId);
        return ResponseEntity.ok(UserWithAdditionalInfoResponse.of(user));
    }

    @GetMapping("/me/chatroom")
    public ResponseEntity<PageableResponse<ChatRoomListResponse>> getUserChatRooms(@UserId Long userId, Pageable pageable) {
        PageableResponse<ChatRoom> chatRoomPageableResponse = userService.getUserChatRooms(userId, pageable);
        return ResponseEntity.ok(PageableResponse.of(chatRoomPageableResponse,
                chatRoomPageableResponse.getContent().stream().map(ChatRoomListResponse::of).toList()));
    }

    @GetMapping("/me/chatroom/{roomId}")
    public ResponseEntity<ChatRoomResponse> getUserChatRoom(@PathVariable("roomId") Long roomId, @UserId Long userId) {
        ChatRoom chatRooms = userService.getUserChatRoom(roomId,userId);
        return ResponseEntity.ok(ChatRoomResponse.of(chatRooms));
    }

    @GetMapping("/me/chatroom/{roomId}/messages")
    public ResponseEntity<PageableResponse<MessageResponse>> getChatMessages(@PathVariable("roomId") Long roomId, @UserId Long userId, Pageable pageable){
        PageableResponse<MessageResponse> response = userService.getChatMessage(roomId, userId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me/email")
    public ResponseEntity<User> getUserByEmail(@RequestParam("email") String email){
        return ResponseEntity.ok(userService.findExistUserByEmail(email));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteUser(@UserId Long userId){
        userService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<Void> test(@UserId Long userId){
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/v1/users/{userId}/username")
    public String getUsername(@PathVariable("userId") Long userId){
        return userService.getUsername(userId);
    }
}
