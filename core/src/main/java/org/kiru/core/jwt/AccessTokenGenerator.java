package org.kiru.core.jwt;

import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AccessTokenGenerator implements TokenGenerator {
    private final JwtProperties jwtProperties;

    @Override
    public String generateToken(final long userId, final String email, Date now){
        final Date expireDate = generateExpirationDate(now);
        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setSubject(String.valueOf(userId))
                .claim("email", email) // email 정보를 Claim에 추가
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(jwtProperties.getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public Date generateExpirationDate(final Date now) {
        return new Date(now.getTime() + jwtProperties.getAccessTokenExpireTime());
    }
}
