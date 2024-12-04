package org.kiru.user.admin.dto;

import java.time.LocalDateTime;
import org.kiru.core.user.user.domain.User;

public record AdminMatchedUserResponse(
        Long userId,
        String name,
        LocalDateTime matchedAt
) {
}
