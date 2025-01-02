package org.kiru.gateway.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.kiru.gateway.common.UserGatewayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class JwtUtils {
    private final UserGatewayRepository userGatewayRepository;
    @Value("${jwt.secret}")
    private String JWT_SECRET;
    private SecretKey signingKey;
    private JwtParser jwtParser;

    @PostConstruct
    public void init() {
        this.signingKey = getSigningKey();
        this.jwtParser = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build();
    }

    @Autowired
    public JwtUtils(UserGatewayRepository userGatewayRepository) {
        this.userGatewayRepository = userGatewayRepository;
    }


    @Cacheable(value = "token", key ="#token", unless = "#result == null")
    public Mono<JwtValidResponse> validateToken(String token) {
        return Mono.fromCallable(() -> {
            try {
                getBody(token);
                Long userId = getUserIdFromToken(token);
                String email = getEmailFromToken(token);
                return userGatewayRepository.findByIdAndEmail(userId, email)
                        .map(JwtValidResponse::of)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("User not found")));
            }  catch (MalformedJwtException ex) {
                return Mono.just(JwtValidResponse.of(JwtValidationType.INVALID_JWT_TOKEN));
            } catch (ExpiredJwtException ex) {
                return  Mono.just(JwtValidResponse.of(JwtValidationType.EXPIRED_JWT_TOKEN));
            } catch (UnsupportedJwtException ex) {
                return  Mono.just(JwtValidResponse.of(JwtValidationType.UNSUPPORTED_JWT_TOKEN));
            } catch (IllegalArgumentException ex) {
                return  Mono.just(JwtValidResponse.of(JwtValidationType.EMPTY_JWT));
            } catch (Exception ex) {
                log.error("Error processing JWT token", ex);
                throw new RuntimeException("Valid Token error occurred");
            }
        }).flatMap(mono -> mono);
    }

    public Long getUserIdFromToken(final String token) {
        return Long.parseLong(getBody(token).getSubject());
    }

    public String getEmailFromToken(final String token) {
        return getBody(token).get("email").toString();
    }

    private Claims getBody(final String token) {
        return jwtParser
                .parseClaimsJws(token)
                .getBody();
    }

    private SecretKey getSigningKey() {
        String encodedKey = Base64.getEncoder().encodeToString(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        return Keys.hmacShaKeyFor(encodedKey.getBytes());
    }
}
