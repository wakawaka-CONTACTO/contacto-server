package org.kiru.core.user.userBlock.domain;


import java.time.LocalDateTime;

public interface UserBlock {
    Long getBlockerId();
    Long getBlockedId();
    LocalDateTime getCreatedAt();


}
