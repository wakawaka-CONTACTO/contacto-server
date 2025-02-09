package org.kiru.gateway.jwt.out;

import org.kiru.gateway.jwt.JwtValidStatusDto;
import reactor.core.publisher.Mono;

public interface GetUserPort {
    Mono<JwtValidStatusDto> getUser(String token,Long userId, String email);

    Mono<JwtValidStatusDto> getUserFromCache(String token);
}