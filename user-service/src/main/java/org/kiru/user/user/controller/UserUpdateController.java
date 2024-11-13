package org.kiru.user.user.controller;

import lombok.RequiredArgsConstructor;
import org.kiru.core.user.user.domain.User;
import org.kiru.user.auth.argumentresolve.UserId;
import org.kiru.user.user.dto.request.UserUpdateDto;
import org.kiru.user.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserUpdateController {
    private final UserService userService;

    @PutMapping(value = "/me", consumes = { "multipart/form-data" })
    public ResponseEntity<User> updateUser(@UserId Long userId, @ModelAttribute UserUpdateDto updatedUser) {
        User user = userService.updateUser(
                userId,
                updatedUser
        );
        return ResponseEntity.ok(user);
    }
}
