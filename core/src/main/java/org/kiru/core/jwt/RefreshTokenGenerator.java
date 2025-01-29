package org.kiru.core.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenGenerator implements TokenGenerator {
    private final JwtProperties jwtProperties;

    @Override
    public String generateToken(final long userId, final String email, Date now) {
        final Date expireDate = new Date(now.getTime() + jwtProperties.getRefreshTokenExpireTime());
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("email", email)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(jwtProperties.getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public Date generateExpirationDate(Date now) {
        return new Date(now.getTime() + jwtProperties.getRefreshTokenExpireTime());
    }
}