package org.kiru.user.user.service.out;

import org.kiru.core.user.user.entity.UserJpaEntity;

public interface UserQueryWithCache {
    UserJpaEntity getUser(Long userId);

    UserJpaEntity saveUser(UserJpaEntity userJpaEntity);
}
