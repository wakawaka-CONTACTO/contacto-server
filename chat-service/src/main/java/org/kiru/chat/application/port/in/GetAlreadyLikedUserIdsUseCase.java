package org.kiru.chat.application.port.in;

import java.util.List;
import org.kiru.chat.adapter.in.web.res.AdminUserResponse;

public interface GetAlreadyLikedUserIdsUseCase {
    List<Long> getAlreadyLikedUserIds(Long userId);
    List<AdminUserResponse> getMatchedUsers(Long userId);
}
