package org.kiru.chat.adapter.in.web.res;

import java.time.LocalDateTime;

public record AdminUserResponse(
        Long userId,
        LocalDateTime matchedAt
) {
}
