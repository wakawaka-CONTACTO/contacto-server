package org.kiru.core.jwt;

import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.SecretKey;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Getter
@Configuration
@RequiredArgsConstructor
public class TokenSecret {
    @Value("${jwt.secret}")
    private String secret;

    public TokenSecret(String secret) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException("시크릿 키는 최소 32자 이상이어야 합니다");
        }
        this.secret = secret;
    }

    @PostConstruct
    public void encodeSecretKeyToBase64() {
        this.secret = Base64.getEncoder()
                .encodeToString(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Bean
    public SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}


