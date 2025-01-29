package org.kiru.core.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.kiru.core.exception.UnauthorizedException;
import org.kiru.core.exception.code.FailureCode;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class JwtTokenParser {
    private JwtParser jwtParser;
    private final JwtProperties jwtProperties;

    @PostConstruct
    public void getJwtParser() {
        this.jwtParser = Jwts.parserBuilder()
                .setSigningKey(jwtProperties.getSigningKey())
                .build();
    }

    public Jws<Claims> parseToken(String token) {
        try {
            return jwtParser.parseClaimsJws(token);
        } catch (ExpiredJwtException e) {
            throw new UnauthorizedException(FailureCode.EXPIRED_ACCESS_TOKEN);
        } catch (UnsupportedJwtException e) {
            throw new UnauthorizedException(FailureCode.UNSUPPORTED_TOKEN_TYPE);
        } catch (MalformedJwtException e) {
            throw new UnauthorizedException(FailureCode.MALFORMED_TOKEN);
        } catch (SignatureException e) {
            throw new UnauthorizedException(FailureCode.INVALID_SIGNATURE_TOKEN);
        } catch (Exception e) {
            throw new UnauthorizedException(FailureCode.INVALID_ACCESS_TOKEN_VALUE);
        }
    }

    public String getEmailFromClaims(Jws<Claims> jws) {
        return jws.getBody().get("email", String.class);
    }

    public Long getUserIdFromClaims(Jws<Claims> jws) {
        String subject = jws.getBody().getSubject();
        try {
            return Long.parseLong(subject);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.valueOf(FailureCode.TOKEN_SUBJECT_NOT_NUMERIC_STRING));
        }
    }

    public String getEmailFromToken(String token) {
        Jws<Claims> jws = parseToken(token);
        return jws.getBody().get("email", String.class);
    }

    public Long getUserIdFromToken(String token) {
        Jws<Claims> jws = parseToken(token);
        String subject = jws.getBody().getSubject();
        try {
            return Long.parseLong(subject);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.valueOf(FailureCode.TOKEN_SUBJECT_NOT_NUMERIC_STRING));
        }
    }


}