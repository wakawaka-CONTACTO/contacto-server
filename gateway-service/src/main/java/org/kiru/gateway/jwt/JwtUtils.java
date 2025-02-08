package org.kiru.gateway.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kiru.core.jwt.JwtTokenParser;
import org.kiru.gateway.jwt.out.GetUserPort;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
@ComponentScan(basePackages = "org.kiru.core.jwt")
public class JwtUtils {
    private final JwtTokenParser jwtTokenParser;
    private final GetUserPort getUserPort;

    public Mono<JwtValidStatusDto> validateToken(String token) {
        return Mono.defer(() -> {
            Jws<Claims> jwt = jwtTokenParser.parseToken(token);
            Long userId = jwtTokenParser.getUserIdFromClaims(jwt);
            String email = jwtTokenParser.getEmailFromClaims(jwt);
            return getUserPort.getUserFromCache(token)
                    .switchIfEmpty(Mono.defer(() -> getUserPort.getUser(token,userId, email)));
        }).cache();
    }
}
