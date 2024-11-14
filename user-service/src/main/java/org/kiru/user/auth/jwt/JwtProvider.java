package org.kiru.user.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.RequiredArgsConstructor;

import org.kiru.user.exception.code.FailureCode;
import org.springframework.stereotype.Component;


@RequiredArgsConstructor
@Component
public class JwtProvider {
    private final JwtGenerator jwtGenerator;
    private final RefreshTokenGenerator refreshTokenGenerator;

    public Token issueToken(final long userId, final String email) {
        return Token.of(
                jwtGenerator.generateAccessToken(userId, email), // email 추가
                refreshTokenGenerator.generateRefreshToken(userId,email)
        );
    }

    public Long getUserIdFromSubject(String token) {
        Jws<Claims> jws = jwtGenerator.parseToken(token);
        String subject = jws.getBody().getSubject();
        try {
            return Long.parseLong(subject);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.valueOf(FailureCode.TOKEN_SUBJECT_NOT_NUMERIC_STRING));
        }
    }

    public String getEmailFromToken(String token) {
        Jws<Claims> jws = jwtGenerator.parseToken(token);
        return jws.getBody().get("email", String.class);
    }
}
