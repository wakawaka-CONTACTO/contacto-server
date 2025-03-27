package org.kiru.user.userBlock.service.out;

import java.util.List;

public interface GetUserBlockQuery {
    List<Long> findAllBlockedUserIdByUserId(Long userId);
    List<Long> findAllBlockedOrBlockingUserByUserIds(Long userId);
}
