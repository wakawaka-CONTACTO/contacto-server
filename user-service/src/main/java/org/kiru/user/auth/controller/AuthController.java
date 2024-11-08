package org.kiru.user.auth.controller;

import lombok.RequiredArgsConstructor;
import org.kiru.user.auth.kakao.KaKaoDto;
import org.kiru.user.auth.kakao.KakaoAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final KakaoAuthService kakaoAuthService;

    @GetMapping("/login/kakao")
    public ResponseEntity<KaKaoDto.KakaoUserInfoResponse> kakaoLogin(
            @RequestParam(value = "code") String code
    ) {
        KaKaoDto.KakaoUserInfoResponse kakaoUserInfoResponse = kakaoAuthService.login(code);
        return ResponseEntity.ok().body(kakaoUserInfoResponse);
    }

// !TODO : 다른 로그인 구현 할때 사용
//    @GetMapping("/login/kakao")
//    public ResponseEntity<KaKaoDto.KakaoUserInfoResponse> kakaoLogin(
//            @RequestParam(value = "code") String code
//    ) {
//        KaKaoDto.KakaoUserInfoResponse kakaoUserInfoResponse = kakaoAuthService.login(code);
//        return ResponseEntity.ok().body( kakaoUserInfoResponse);
//    }
}
