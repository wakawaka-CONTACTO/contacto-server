package org.kiru.user.user.dto.event;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record TokenCreateEvent(
    String token,
    Long userId,
    LocalDateTime expiredAt
)  {
}
