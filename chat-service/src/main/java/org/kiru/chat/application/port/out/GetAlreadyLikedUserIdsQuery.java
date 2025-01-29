package org.kiru.chat.application.port.out;

import jakarta.persistence.QueryHint;
import java.util.List;
import org.kiru.chat.adapter.in.web.res.AdminUserResponse;
import org.springframework.data.jpa.repository.QueryHints;

public interface GetAlreadyLikedUserIdsQuery {
    List<Long> getAlreadyLikedUserIds(Long userId);

    @QueryHints(value = {
            @QueryHint(name = "org.hibernate.readOnly", value = "true"),
            @QueryHint(name = "org.hibernate.fetchSize", value = "150"),
            @QueryHint(name = "jakarta.persistence.query.timeout", value = "5000")

    }
    )
    List<AdminUserResponse> getMatchedUsers(Long userId);
}
