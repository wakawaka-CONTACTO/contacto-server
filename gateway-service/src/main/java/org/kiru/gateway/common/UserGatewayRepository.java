package org.kiru.gateway.common;

import org.kiru.core.user.user.domain.User;
import org.kiru.core.user.user.entity.UserR2dbcEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

/**
 * Repository for user gateway
 */
@Repository
public interface UserGatewayRepository extends ReactiveCrudRepository<UserR2dbcEntity,Long> {
    @Query("SELECT * FROM users WHERE id = :userId AND email = :email")
    Mono<UserR2dbcEntity> findByIdAndEmailNoCache(Long userId, String email);

    default Mono<User> findByIdAndEmail(Long userId, String email) {
        return findByIdAndEmailNoCache(userId, email)
                .map(User::of);
    }
}
