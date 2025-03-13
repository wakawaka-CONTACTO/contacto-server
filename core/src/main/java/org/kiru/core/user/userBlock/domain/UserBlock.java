package org.kiru.core.user.userBlock.domain;


import java.time.LocalDateTime;

public interface UserBlock {
    Long getUserId();
    Long getBlockedId();
    LocalDateTime getCreatedAt();
}
