package org.kiru.user.user.controller;


import static org.springframework.cloud.openfeign.security.OAuth2AccessTokenInterceptor.AUTHORIZATION;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.kiru.user.user.dto.request.UserPurposesReq;
import org.kiru.user.user.dto.request.UserSignInReq;
import org.kiru.user.user.dto.request.UserSignUpReq;
import org.kiru.user.user.dto.request.UserTalentsReq;
import org.kiru.user.user.dto.response.UserJwtInfoRes;
import org.kiru.user.user.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserLoginController {
    private final AuthService authService;

//    회원가입
    @PostMapping(value = "/signup",consumes = {MediaType.MULTIPART_FORM_DATA_VALUE,
            MediaType.APPLICATION_JSON_VALUE}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserJwtInfoRes> signUp(@RequestPart("userSignUpReq") final UserSignUpReq userSignUPReq,
                                                 @RequestPart("portfolioImgs") final List<MultipartFile> images,
                                                 @RequestPart("purpose") final List<UserPurposesReq> purposes,
                                                 @RequestPart("talent") final List<UserTalentsReq> talents
    ) {
        UserJwtInfoRes userSignUpRes = authService.signUp(userSignUPReq, images, purposes,talents);
        return ResponseEntity.status(HttpStatus.CREATED).body(userSignUpRes);
    }
    //로그인
    @PostMapping("/signin")
    public ResponseEntity<UserJwtInfoRes> signIn(@RequestHeader(AUTHORIZATION) final String token,
                                                 @RequestBody final UserSignInReq userSignInReq) {
        UserJwtInfoRes userSignInRes = authService.signIn(token, userSignInReq);
        return ResponseEntity.status(HttpStatus.CREATED).body(userSignInRes);
    }
//    !TODO
//    @DeleteMapping("/signout")
//    public ResponseEntity<Void> signout(@UserId final Long userId) {
//        authService.signout(userId);
//        return ResponseEntity.ok().build();
//    }
}

//https://kauth.kakao.com/oauth/authorize?client_id=REST_API_KEY입력&redirect_uri=http://localhost:8080/app/login/kakao&response_type=code