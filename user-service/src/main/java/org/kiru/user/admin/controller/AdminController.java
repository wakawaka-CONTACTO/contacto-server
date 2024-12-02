package org.kiru.user.admin.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.kiru.core.chat.chatroom.domain.ChatRoom;
import org.kiru.core.user.user.domain.User;
import org.kiru.user.admin.dto.AdminMatchedUserResponse;
import org.kiru.user.admin.dto.AdminUserDto;
import org.kiru.user.admin.dto.MatchedUserResponse;
import org.kiru.user.admin.service.AdminService;
import org.kiru.user.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
    private final UserService userService;
    @GetMapping("/users")
    public ResponseEntity<Page<AdminUserDto>> getUsers(Pageable pageable) {
        Page<AdminUserDto> users = adminService.getUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users")
    public ResponseEntity<AdminUserDto> findUserByName(@RequestParam String name) {
        AdminUserDto users = adminService.findUserByName(name);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<User> getUserDetail(@PathVariable Long userId) {
        User user = userService.getUserFromIdToMainPage(userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/users/{userId}/matched")
    public ResponseEntity<List<AdminMatchedUserResponse>> getMatchedUsers(@PathVariable Long userId) {
        List<MatchedUserResponse> userIds = adminService.getAlreadyLikedUserIds(userId);
        return  adminService.getUsersByIds(userIds);
    }
}
