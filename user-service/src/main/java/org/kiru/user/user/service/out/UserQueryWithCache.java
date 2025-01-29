package org.kiru.user.user.service.out;

import org.kiru.core.user.user.domain.User;

public interface UserQueryWithCache {
    User getUser(Long userId);
    User saveUser(User user);
}
