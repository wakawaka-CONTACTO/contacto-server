package org.kiru.gateway.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import java.util.Base64;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.kiru.core.user.user.entity.UserJpaEntity;
import org.kiru.gateway.common.UserGateWayRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtUtils {
    @Value("${jwt.secret}")
    private String JWT_SCRET;
    private final UserGateWayRepository userGateWayRepository;

    public JwtValidResponse validateToken(String token) {
        try {
            final Claims claims = getBody(token);
            Long userId = getUserIdFromToken(token);
            String email = getEmailFromToken(token);
            UserJpaEntity user = userGateWayRepository.findByIdAndEmail(userId, email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            return JwtValidResponse.of(user);
        } catch (MalformedJwtException ex) {
            return JwtValidResponse.of(JwtValidationType.INVALID_JWT_TOKEN);
        } catch (ExpiredJwtException ex) {
            return JwtValidResponse.of(JwtValidationType.EXPIRED_JWT_TOKEN);
        } catch (UnsupportedJwtException ex) {
            return JwtValidResponse.of(JwtValidationType.UNSUPPORTED_JWT_TOKEN);
        } catch (IllegalArgumentException ex) {
            return JwtValidResponse.of(JwtValidationType.EMPTY_JWT);
        }
    }

    public Long getUserIdFromToken(final String token){
        return Long.parseLong(getBody(token).getSubject());
    }
    public String getEmailFromToken(String token) {
        return getBody(token).get("email", String.class);
    }

    private Claims getBody(final String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private SecretKey getSigningKey() {
        String encodedKey = Base64.getEncoder().encodeToString(JWT_SCRET.getBytes()); //SecretKey 통해 서명 생성
        return Keys.hmacShaKeyFor(encodedKey.getBytes());   //일반적으로 HMAC (Hash-based Message Authentication Code) 알고리즘 사용
    }
}
