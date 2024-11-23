package org.kiru.chat.application.port.in;

import java.util.List;

public interface GetAlreadyLikedUserIdsUseCase {
    List<Long> getAlreadyLikedUserIds(Long userId);
}
