package org.kiru.gateway.common;

import java.util.Optional;
import org.kiru.core.user.user.entity.UserJpaEntity;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserGateWayRepository extends JpaRepository<UserJpaEntity, Long> {
    @Cacheable(cacheNames = "user", key = "#userId", unless = "#result == null")
    Optional<UserJpaEntity> findByIdAndEmail(Long userId, String email);
}
