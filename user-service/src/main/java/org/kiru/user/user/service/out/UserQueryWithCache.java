package org.kiru.user.user.service.out;

import org.kiru.core.user.user.domain.User;
import org.kiru.core.user.user.entity.UserJpaEntity;

public interface UserQueryWithCache {
    User getUser(Long userId);

    User saveUser(UserJpaEntity userJpaEntity);
}
