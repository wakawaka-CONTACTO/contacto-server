package org.kiru.user.auth.controller;

import org.kiru.user.auth.kakao.KaKaoDto;
import org.kiru.user.auth.kakao.KakaoAuthService;
import org.kiru.user.user.dto.response.UserJwtInfoRes;
import org.kiru.user.user.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final KakaoAuthService kakaoAuthService;
    private final AuthService authService;

    @Deprecated
    @GetMapping("/login/kakao")
    public ResponseEntity<KaKaoDto.KakaoUserInfoResponse> kakaoLogin(
            @RequestParam(value = "code") String code
    ) {
        KaKaoDto.KakaoUserInfoResponse kakaoUserInfoResponse = kakaoAuthService.login(code);
        return ResponseEntity.ok().body(kakaoUserInfoResponse);
    }

    @PostMapping("/reissue")
    public ResponseEntity<UserJwtInfoRes> reissueToken(@RequestHeader("Refreshtoken") String refreshToken) {
        UserJwtInfoRes newTokens = authService.reissue(refreshToken);
        return ResponseEntity.ok(newTokens);
    }
}
