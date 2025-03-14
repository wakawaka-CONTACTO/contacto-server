package org.kiru.user.userBlock.service.out;

import org.kiru.core.user.userBlock.domain.UserBlock;

public interface BlockUserQuery {
    UserBlock blockUser(Long userId, Long blockedUserId);
}
