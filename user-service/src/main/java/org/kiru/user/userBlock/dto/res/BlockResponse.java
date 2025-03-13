package org.kiru.user.userBlock.dto.res;

import lombok.Builder;
import lombok.Getter;
import org.kiru.core.user.userBlock.domain.UserBlock;

@Builder
@Getter
public class BlockResponse {
    Long userId;
    Long blockedId;

    public static BlockResponse of(UserBlock userBlock) {
        return BlockResponse.builder()
                .userId(userBlock.getUserId())
                .blockedId(userBlock.getBlockedId())
                .build();
    }
}
