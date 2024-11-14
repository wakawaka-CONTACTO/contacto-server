package org.kiru.user.user.repository;

import java.util.Optional;
import org.kiru.core.user.user.entity.UserJpaEntity;

public interface UserCacheRepository {
    Optional<UserJpaEntity> findByIdWithCache(Long userId);
}
