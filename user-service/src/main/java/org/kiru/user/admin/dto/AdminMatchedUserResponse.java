package org.kiru.user.admin.dto;

import java.time.LocalDateTime;
import org.kiru.core.user.user.domain.User;

public record AdminMatchedUserResponse(
        String name,
        LocalDateTime localDateTime
) {
    public static AdminMatchedUserResponse of(User user) {
        return new AdminMatchedUserResponse(user.getUsername(), user.creat);
    }
}
