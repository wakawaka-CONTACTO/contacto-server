package org.kiru.gateway.adapter;

import lombok.RequiredArgsConstructor;
import org.kiru.core.exception.EntityNotFoundException;
import org.kiru.core.exception.UnauthorizedException;
import org.kiru.core.exception.code.FailureCode;
import org.kiru.gateway.common.UserGatewayRepository;
import org.kiru.gateway.jwt.JwtValidStatusDto;
import org.kiru.gateway.jwt.out.GetUserPort;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class UserAdapter implements GetUserPort {
    private final UserGatewayRepository userGatewayRepository;
    private final ReactiveRedisOperations<String, JwtValidStatusDto> redisTemplate;

    @Override
    public Mono<JwtValidStatusDto> getUser(String token, Long userId, String email) {
        return userGatewayRepository.findByIdAndEmail(userId, email)
                .map(JwtValidStatusDto::of)
                .flatMap(dto -> redisTemplate.opsForValue().set(token, dto).thenReturn(dto))
                .switchIfEmpty(Mono.defer(() -> Mono.error(new UnauthorizedException(FailureCode.UNAUTHORIZED))));
    }

    @Override
    public Mono<JwtValidStatusDto> getUserFromCache(String token) {
        return redisTemplate.opsForValue().get(token)
                .switchIfEmpty(Mono.empty());
    }
}