package org.kiru.chat.application.port.out;

import java.util.List;

public interface GetAlreadyLikedUserIdsQuery {
    List<Long> getAlreadyLikedUserIds(Long userId);
}
