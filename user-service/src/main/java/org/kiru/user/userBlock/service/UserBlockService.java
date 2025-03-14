package org.kiru.user.userBlock.service;

import org.kiru.core.user.userBlock.domain.UserBlock;
import org.kiru.user.userBlock.dto.res.BlockResponse;
import org.kiru.user.userBlock.service.out.BlockUserQuery;
import org.springframework.stereotype.Service;

@Service
public class UserBlockService {
    private final BlockUserQuery blockUserQuery;
    public UserBlockService(BlockUserQuery blockUserQuery) {
        this.blockUserQuery = blockUserQuery;
    }
    public BlockResponse blockUser(Long userId, Long blockedUserId) {
        UserBlock userBlock = blockUserQuery.blockUser(userId, blockedUserId);
        return BlockResponse.of(userBlock);
    }
}
