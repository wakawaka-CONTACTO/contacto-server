package org.kiru.user.auth.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.time.ZoneId;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.kiru.core.user.refreshtoken.RefreshToken;
import org.kiru.user.auth.jwt.refreshtoken.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenGenerator {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;
    private final JwtGenerator jwtGenerator;

    public String generateRefreshToken(final long userId, final String email) {
        final Date now = new Date();
        final Date expireDate = new Date(now.getTime() + jwtProperties.getRefreshTokenExpireTime());

        String token = Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("email", email)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(jwtGenerator.getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
        RefreshToken newRefreshToken = RefreshToken.create(token, userId, expireDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        refreshTokenRepository.save(newRefreshToken);
        return token;
    }
}