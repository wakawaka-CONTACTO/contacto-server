package org.kiru.user.userBlock.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kiru.core.exception.BadRequestException;
import org.kiru.core.exception.code.FailureCode;
import org.kiru.user.auth.argumentresolve.UserId;
import org.kiru.user.userBlock.dto.res.BlockResponse;
import org.kiru.user.userBlock.service.UserBlockService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/users/blocks")
@RequiredArgsConstructor
public class UserBlockController {
    private final UserBlockService userBlockService;

    @PostMapping("/{blockedId}")
    public ResponseEntity<BlockResponse> blockUser(
            @UserId Long userId,
            @PathVariable Long blockedId) {
        log.info("Blocking user {} for user {}", blockedId, userId);
        if(userId.equals(blockedId)) {
            throw new BadRequestException(FailureCode.INVALID_USER_BLOCK);
        }
        BlockResponse blockResponse = userBlockService.blockUser(userId, blockedId);
        return ResponseEntity.status(HttpStatus.CREATED).body(blockResponse);
    }
}
