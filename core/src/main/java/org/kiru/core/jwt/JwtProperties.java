package org.kiru.core.jwt;

import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.SecretKey;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.kiru.core.jwt.JwtProperties.JwtPropertiy;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Getter
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(JwtPropertiy.class)
public class JwtProperties {
    private final JwtPropertiy jwtPropertiy;
    private String encodeSecretKeyToBase64;

    @ConfigurationProperties("jwt")
    @RequiredArgsConstructor
    @Getter
    public static class JwtPropertiy{
        private final long refreshTokenExpireTime;
        private final long accessTokenExpireTime;
        private final String secret;
    }

    @PostConstruct
    public void encodeSecretKeyToBase64() {
        this.encodeSecretKeyToBase64 = Base64.getEncoder().encodeToString(jwtPropertiy.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    @Bean
    public SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(encodeSecretKeyToBase64.getBytes());
    }

    @Bean
    public long getAccessTokenExpireTime() {
        return jwtPropertiy.getAccessTokenExpireTime();
    }

    @Bean
    public long getRefreshTokenExpireTime() {
        return jwtPropertiy.getRefreshTokenExpireTime();
    }
}


