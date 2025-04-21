package org.kiru.user.userlike.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kiru.core.exception.BadRequestException;
import org.kiru.core.exception.code.FailureCode;
import org.kiru.user.auth.argumentresolve.UserId;
import org.kiru.user.userlike.dto.req.LikeRequest;
import org.kiru.user.userlike.dto.res.LikeLimitResponse;
import org.kiru.user.userlike.dto.res.LikeResponse;
import org.kiru.user.userlike.service.UserLikeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/users/likes")
@RequiredArgsConstructor
public class UserLikeController {
    private final UserLikeService userLikeService;

    @GetMapping
    public ResponseEntity<LikeLimitResponse> getLikeLimit(@UserId Long userId) {
        LikeLimitResponse likeLimitResponse = userLikeService.getLikeLimit(userId);
        return ResponseEntity.status(HttpStatus.OK).body(likeLimitResponse);
    }

    @PostMapping
    public ResponseEntity<LikeResponse> sendLikeOrDislike(
            @UserId Long userId,
            @RequestBody LikeRequest likeRequest) {
        log.info("User {} sending like or dislike to user {}", userId, likeRequest.likedUserId());
        if(userId.equals(likeRequest.likedUserId())) {
            throw new BadRequestException(FailureCode.INVALID_USER_BLOCK);
        }
        LikeResponse likeResponse = userLikeService.sendLikeOrDislike(userId, likeRequest.likedUserId(), likeRequest.status());
        return ResponseEntity.status(HttpStatus.CREATED).body(likeResponse);
    }
}
