package org.kiru.user.user.controller;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.kiru.user.auth.argumentresolve.UserId;
import org.kiru.user.user.dto.request.*;
import org.kiru.user.user.dto.response.SignHelpDtoRes;
import org.kiru.user.user.dto.response.UserJwtInfoRes;
import org.kiru.user.user.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserLoginController {
    private final AuthService authService;

    @PostMapping(value = "/signup", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE,
            MediaType.APPLICATION_JSON_VALUE}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserJwtInfoRes> signUp(
            @RequestPart("userSignUpReq") @Valid final UserSignUpReq userSignUpReq,
            @RequestPart("portfolioImgs") @Valid @Size(max = 9) final List<MultipartFile> images,
            @RequestPart("purpose")  @Valid @NotEmpty final List<UserPurposesReq> purposes,
            @RequestPart("talent") @Valid @NotEmpty  final List<UserTalentsReq> talents
    ) {
        UserJwtInfoRes userSignUpRes = authService.signUp(userSignUpReq, images, purposes, talents);
        return ResponseEntity.status(HttpStatus.CREATED).body(userSignUpRes);
    }

    //로그인
    @PostMapping("/signin")
    public ResponseEntity<UserJwtInfoRes> signIn(
            @RequestBody final UserSignInReq userSignInReq
    ) {
        UserJwtInfoRes userSignInRes = authService.signIn(userSignInReq);

        return ResponseEntity.status(HttpStatus.CREATED).body(userSignInRes);
    }

    @PostMapping("/signin/help") // 이 부분은 각자 바꿔주시면 됩니다.
    public ResponseEntity<SignHelpDtoRes> signHelp(@RequestBody SignHelpDto signHelpDto) {
        return  ResponseEntity.ok(authService.signHelp(signHelpDto));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @UserId Long userId,
            @RequestBody final LogoutReq logoutReq
    ) {
        authService.logout(userId, logoutReq.deviceId());
        return ResponseEntity.ok().build();
    }

}