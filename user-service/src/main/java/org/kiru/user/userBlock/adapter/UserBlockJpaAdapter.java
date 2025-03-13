package org.kiru.user.userBlock.adapter;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.kiru.core.user.userBlock.domain.UserBlock;
import org.kiru.core.user.userBlock.entity.UserBlockJpaEntity;
import org.kiru.user.userBlock.repository.UserBlockJpaRepository;
import org.kiru.user.userBlock.service.out.BlockUserQuery;
import org.kiru.user.userBlock.service.out.GetUserBlockQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Repository
@Transactional(readOnly = true)
public class UserBlockJpaAdapter implements BlockUserQuery, GetUserBlockQuery {
    private final UserBlockJpaRepository userBlockRepository;

    @Transactional
    @Override
    public UserBlock blockUser(Long userId, Long blockedId) {
        UserBlock userBlock = userBlockRepository.findByUserIdAndBlockedId(userId, blockedId)
                .orElseGet(() -> UserBlockJpaEntity.of(userId, blockedId));
        userBlockRepository.save(userBlock);
        return userBlock;
    }

    @Override
    public List<Long> findAllBlockedIdByUserId(Long userId) {
        List<Long> blockedUserIds = userBlockRepository.findAllBlockedIdByUserId(userId);
        return blockedUserIds;
    }
}
