package org.kiru.core.jwt;

import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.SecretKey;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.kiru.core.jwt.JwtProperties.JwtProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Getter
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(JwtProperty.class)
public class JwtProperties {
    private final JwtProperty jwtProperty;
    private String encodeSecretKeyToBase64;

    @PostConstruct
    public void encodeSecretKeyToBase64() {
        this.encodeSecretKeyToBase64 = Base64.getEncoder()
                .encodeToString(jwtProperty.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    @Bean
    public SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(encodeSecretKeyToBase64.getBytes());
    }

    @Bean
    public long getAccessTokenExpireTime() {
        return jwtProperty.getAccessTokenExpireTime();
    }

    @Bean
    public long getRefreshTokenExpireTime() {
        return jwtProperty.getRefreshTokenExpireTime();
    }

    @ConfigurationProperties("jwt")
    @Getter
    public static class JwtProperty {
        private final long refreshTokenExpireTime;
        private final long accessTokenExpireTime;
        private final String secret;

        public JwtProperty(long refreshTokenExpireTime, long accessTokenExpireTime, String secret) {
            if (refreshTokenExpireTime <= 0) {
                throw new IllegalArgumentException("리프레시 토큰 만료 시간은 0보다 커야 합니다");
            }
            if (accessTokenExpireTime <= 0) {
                throw new IllegalArgumentException("액세스 토큰 만료 시간은 0보다 커야 합니다");
            }
            if (secret == null || secret.length() < 32) {
                throw new IllegalArgumentException("시크릿 키는 최소 32자 이상이어야 합니다");
            }
            this.refreshTokenExpireTime = refreshTokenExpireTime;
            this.accessTokenExpireTime = accessTokenExpireTime;
            this.secret = secret;
        }
    }
}


