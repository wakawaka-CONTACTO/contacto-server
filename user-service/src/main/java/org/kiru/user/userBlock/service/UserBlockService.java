package org.kiru.user.userBlock.service;

import lombok.extern.slf4j.Slf4j;
import org.kiru.core.user.userBlock.domain.UserBlock;
import org.kiru.user.userBlock.dto.res.BlockResponse;
import org.kiru.user.userBlock.service.out.BlockUserQuery;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserBlockService {
    private final BlockUserQuery blockUserQuery;
    public UserBlockService(BlockUserQuery blockUserQuery) {
        this.blockUserQuery = blockUserQuery;
    }
    public BlockResponse blockUser(Long userId, Long blockedId) {
        UserBlock userBlock = blockUserQuery.blockUser(userId, blockedId);
        return BlockResponse.of(userBlock);
    }
}
