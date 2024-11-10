package org.kiru.user.user.controller;



import jakarta.validation.constraints.NotNull;
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
    public ResponseEntity<UserJwtInfoRes> signUp(@RequestPart("userSignUpReq") @NotNull final UserSignUpReq userSignUpReq,
                                                 @RequestPart("portfolioImgs") @NotNull final List<MultipartFile> images,
                                                 @RequestPart("purpose")  @NotNull final List<UserPurposesReq> purposes,
                                                 @RequestPart("talent")  @NotNull final List<UserTalentsReq> talents
    ) {
        UserJwtInfoRes userSignUpRes = authService.signUp(userSignUpReq, images, purposes,talents);
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
}