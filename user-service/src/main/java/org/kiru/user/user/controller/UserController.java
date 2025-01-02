package org.kiru.user.user.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.kiru.core.chat.chatroom.domain.ChatRoom;
import org.kiru.core.user.user.domain.User;
import org.kiru.user.auth.argumentresolve.UserId;
import org.kiru.user.user.dto.response.ChatRoomListResponse;
import org.kiru.user.user.dto.response.ChatRoomResponse;
import org.kiru.user.user.service.UserService;
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
public class UserController {
    private final UserService userService;
    @GetMapping("/me")
    public ResponseEntity<User> getUser(@UserId Long userId){
        User user = userService.getUserFromIdToMainPage(userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/me/chatroom")
    public ResponseEntity<List<ChatRoomListResponse>> getUserChatRooms(@UserId Long userId, Pageable pageable){
        List<ChatRoom> chatRooms = userService.getUserChatRooms(userId,pageable);
        return ResponseEntity.ok(chatRooms.stream().map(ChatRoomListResponse::of).toList());
    }

    @GetMapping("/me/chatroom/{roomId}")
    public ResponseEntity<ChatRoomResponse> getUserChatRoom(@PathVariable Long roomId, @UserId Long userId) {
        ChatRoom chatRooms = userService.getUserChatRoom(roomId,userId);
        return ResponseEntity.ok(ChatRoomResponse.of(chatRooms));
    }

    @GetMapping("/me/email")
    public ResponseEntity<User> getUser(@RequestParam("email") String email){
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
}
