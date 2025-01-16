package org.kiru.user.userlike.controller;


import lombok.RequiredArgsConstructor;
import org.kiru.core.exception.BadRequestException;
import org.kiru.core.exception.code.FailureCode;
import org.kiru.user.auth.argumentresolve.UserId;
import org.kiru.user.userlike.dto.req.LikeRequest;
import org.kiru.user.userlike.dto.res.LikeResponse;
import org.kiru.user.userlike.service.UserLikeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/likes")
@RequiredArgsConstructor
public class UserLikeController {
    private final UserLikeService userLikeService;

    @PostMapping
    public ResponseEntity<LikeResponse> sendLikeOrDislike(
            @UserId Long userId,
            @RequestBody LikeRequest likeRequest) {
        if(userId.equals(likeRequest.likedUserId())) {
            throw new BadRequestException(FailureCode.INVALID_USER_LIKE);
        }
        LikeResponse likeResponse = userLikeService.sendLikeOrDislike(userId, likeRequest.likedUserId(), likeRequest.status());
        return  ResponseEntity.status(HttpStatus.CREATED).body(likeResponse);
    }
}
