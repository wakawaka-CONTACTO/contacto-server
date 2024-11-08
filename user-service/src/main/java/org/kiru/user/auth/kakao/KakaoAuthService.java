package org.kiru.user.auth.kakao;

import lombok.RequiredArgsConstructor;
import org.kiru.user.auth.api.KakaoApiClient;
import org.kiru.user.auth.api.KakaoAuthApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;



@Service
@RequiredArgsConstructor
public class KakaoAuthService {
    @Value("${kakao.client_id}")
    private String clientId;
    private String KAUTH_TOKEN_URL_HOST = "https://kauth.kakao.com";
    private String KAUTH_USER_URL_HOST = "https://kapi.kakao.com";

    private final KakaoAuthApi kakaoAuthApi;
    private final KakaoApiClient kakaoApi;

    public KaKaoDto.KakaoUserInfoResponse login(
            final String authorizationCode
    ){
        KakaoTokenResponse tokenResponse = getAccessTokenFromKakao(authorizationCode);
        return getUserInfo(tokenResponse.getAccessToken());
    }
    public KakaoTokenResponse getAccessTokenFromKakao(String code) {
        return kakaoAuthApi.getOAuth2AccessToken("authorization_code",clientId,"http://localhost:8082/onboarding.html",code);
    }
    public KaKaoDto.KakaoUserInfoResponse getUserInfo(
            final String accessToken
    ){
        return kakaoApi.getUserInformation("Bearer " + accessToken);
    }
}
