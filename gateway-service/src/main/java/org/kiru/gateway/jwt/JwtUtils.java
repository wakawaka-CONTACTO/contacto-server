package org.kiru.gateway.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kiru.core.exception.EntityNotFoundException;
import org.kiru.core.exception.code.FailureCode;
import org.kiru.core.jwt.JwtTokenParser;
import org.kiru.gateway.common.UserGatewayRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
@ComponentScan(basePackages = "org.kiru.core.jwt")
public class JwtUtils {
    private final UserGatewayRepository userGatewayRepository;
    private final JwtTokenParser jwtTokenParser;

    @Cacheable(value = "token", key = "#token", unless = "#result == null")
    public Mono<JwtValidResponse> validateToken(String token) {
        return Mono.fromCallable(() -> {
            Jws<Claims> jwt = jwtTokenParser.parseToken(token);
            Long userId = jwtTokenParser.getUserIdFromClaims(jwt);
            String email = jwtTokenParser.getEmailFromClaims(jwt);
            return userGatewayRepository.findByIdAndEmail(userId, email)
                    .map(JwtValidResponse::of)
                    .switchIfEmpty(Mono.error(new EntityNotFoundException(FailureCode.USER_NOT_FOUND)));
        }).flatMap(mono -> mono);
    }
}
