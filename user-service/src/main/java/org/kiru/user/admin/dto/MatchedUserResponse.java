package org.kiru.user.admin.dto;

import java.time.LocalDateTime;

public record MatchedUserResponse(
        Long userId,
        LocalDateTime matchedAt
) {
}
