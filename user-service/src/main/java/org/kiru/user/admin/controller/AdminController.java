package org.kiru.user.admin.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.kiru.core.chat.chatroom.domain.ChatRoom;
import org.kiru.core.chat.message.domain.Message;
import org.kiru.core.user.user.domain.User;
import org.kiru.user.admin.dto.AdminLikeUserResponse;
import org.kiru.user.admin.dto.AdminMatchedUserResponse;
import org.kiru.user.admin.dto.AdminUserDto;
import org.kiru.user.admin.service.AdminService;
import org.kiru.user.auth.argumentresolve.UserId;
import org.kiru.user.user.dto.response.ChatRoomListResponse;
import org.kiru.user.user.dto.response.ChatRoomResponse;
import org.kiru.user.user.service.UserService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
    private final UserService userService;

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserDto>> getUsers(Pageable pageable) {
        List<AdminUserDto> users = adminService.getUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/search")
    public ResponseEntity<List<AdminUserDto>> findUserByName(@RequestParam String name ,Pageable pageable) {
        List<AdminUserDto> users = adminService.findUserByName(name,pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<User> getUserDetail(@PathVariable Long userId) {
        User user = userService.getUserFromIdToMainPage(userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/users/{userId}/matched")
    public ResponseEntity<List<AdminMatchedUserResponse>> getMatchedUsers(@PathVariable Long userId, Pageable pageable) {
        List<AdminMatchedUserResponse> matchedUsers = adminService.getMatchedUsers(userId,pageable);
        return  ResponseEntity.ok(matchedUsers);
    }

    @GetMapping("/chatroom")
    public ResponseEntity<List<ChatRoomListResponse>> getUserChatRooms(@RequestParam Long userId, Pageable pageable){
        List<ChatRoom> chatRooms = userService.getUserChatRooms(userId,pageable);
        return ResponseEntity.ok(chatRooms.stream().map(ChatRoomListResponse::of).toList());
    }

    @GetMapping("/rooms/{roomId}")
    public ChatRoom getRoom(@PathVariable Long roomId, @UserId Long userId) {
        return adminService.getRoom(roomId, userId);
    }

    @GetMapping("/users/{userId}/likes")
    public ResponseEntity<AdminLikeUserResponse> getUserLikes(Pageable pageable, @PathVariable Long userId, @RequestParam(required = false) String name) {
        AdminLikeUserResponse adminLikeUserResponse;
        if (name != null && !name.isEmpty()) {
            adminLikeUserResponse = adminService.getUserLikesAndUserLikedByName(pageable, userId, name);
        } else {
            adminLikeUserResponse = adminService.getUserLikesAndUserLiked(pageable, userId);
        }
        return ResponseEntity.ok(adminLikeUserResponse);
    }

    @GetMapping("/cs")
    public ResponseEntity<ChatRoomResponse> getUserLikes(@UserId Long adminId, @RequestParam Long userId) {
        ChatRoom chatRooms = adminService.getOrCreateCsChatRoom(userId,adminId);
        return ResponseEntity.ok(ChatRoomResponse.of(chatRooms));
    }

    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<Slice<Message>> getMessages(@PathVariable Long roomId, @UserId Long userId, Pageable pageable) {
        Slice<Message> messages = adminService.getMessages(roomId, userId, true, pageable);
        return ResponseEntity.ok(messages);
    }
}
