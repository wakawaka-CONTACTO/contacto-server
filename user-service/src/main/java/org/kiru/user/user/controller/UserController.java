package org.kiru.user.user.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.kiru.core.chatroom.domain.ChatRoom;
import org.kiru.core.user.domain.User;
import org.kiru.user.auth.argumentresolve.UserId;
import org.kiru.user.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public ResponseEntity<List<ChatRoom>> getUserChatRooms(@UserId Long userId){
        List<ChatRoom> chatRooms = userService.getUserChatRooms(userId);
        return ResponseEntity.ok(chatRooms);
    }

    @GetMapping("/me/chatroom/{roomId}")
    public ResponseEntity<ChatRoom> getUserChatRoom(@PathVariable Long roomId, @UserId Long userId) {
        ChatRoom chatRooms = userService.getUserChatRoom(roomId,userId);
        return ResponseEntity.ok(chatRooms);
    }
}
