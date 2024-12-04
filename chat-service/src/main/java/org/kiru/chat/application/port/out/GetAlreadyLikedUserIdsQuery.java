package org.kiru.chat.application.port.out;

import java.util.List;
import org.kiru.chat.adapter.in.web.res.AdminUserResponse;

public interface GetAlreadyLikedUserIdsQuery {
    List<Long> getAlreadyLikedUserIds(Long userId);

    List<AdminUserResponse> getMatchedUsers(Long userId); 
}
