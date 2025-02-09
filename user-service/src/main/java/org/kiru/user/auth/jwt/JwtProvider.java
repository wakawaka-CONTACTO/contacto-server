package org.kiru.user.auth.jwt;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.kiru.core.jwt.TokenGenerator;
import org.kiru.user.user.dto.event.TokenCreateEvent;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;


@RequiredArgsConstructor
@Component
@ComponentScan(basePackages = "org.kiru.core.jwt")
@ConfigurationPropertiesScan(basePackages = {"org.kiru.core.jwt"})
public class JwtProvider {
    private final TokenGenerator accessTokenGenerator;
    private final TokenGenerator refreshTokenGenerator;
    private final ApplicationEventPublisher applicationEventPublisher;

    public Token issueToken(final long userId, final String email, final Date now) {
        Token token =  Token.of(
                accessTokenGenerator.generateToken(userId, email, now), // email 추가
                refreshTokenGenerator.generateToken(userId, email, now)
        );
        applicationEventPublisher.publishEvent(TokenCreateEvent.builder()
                .userId(userId)
                .expiredAt(toLocalDateTime(refreshTokenGenerator.generateExpirationDate(now)))
                .token(token.refreshToken()).build());
        return token;
    }

    public LocalDateTime toLocalDateTime(Date expiredAt) {
        return expiredAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
