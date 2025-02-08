package org.kiru.core.jwt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kiru.core.jwt.JwtProperties.TokenExpireTimeProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Slf4j
@Getter
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(TokenExpireTimeProperty.class)
public class JwtProperties {
    private final TokenExpireTimeProperty tokenExpireTimeProperty;

    @Bean
    public long getAccessTokenExpireTime() {
        return tokenExpireTimeProperty.getAccessTokenExpireTime();
    }

    @Bean
    public long getRefreshTokenExpireTime() {
        return tokenExpireTimeProperty.getRefreshTokenExpireTime();
    }

    @ConfigurationProperties("jwt")
    @Getter
    public static class TokenExpireTimeProperty {
        private final long refreshTokenExpireTime;
        private final long accessTokenExpireTime;

        public TokenExpireTimeProperty(long refreshTokenExpireTime, long accessTokenExpireTime) {
            if (refreshTokenExpireTime < 0) {
                log.info("리프레시 토큰 :{}", refreshTokenExpireTime);
                throw new IllegalArgumentException("리프레시 토큰 만료 시간은 0보다 커야 합니다");
            }
            if (accessTokenExpireTime < 0) {
                log.info("액세스 토큰 {}", accessTokenExpireTime);
                throw new IllegalArgumentException("액세스 토큰 만료 시간은 0보다 커야 합니다");
            }

            this.refreshTokenExpireTime = refreshTokenExpireTime;
            this.accessTokenExpireTime = accessTokenExpireTime;
        }
    }
}


