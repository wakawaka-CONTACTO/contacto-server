package org.kiru.user.auth.jwt.refreshtoken.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.kiru.core.user.refreshtoken.QRefreshToken;
import org.kiru.core.user.refreshtoken.RefreshToken;

@RequiredArgsConstructor
public class RefreshTokenAdapter implements RefreshTokenRepositoryCustom{
    private final JPAQueryFactory queryFactory;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public Optional<RefreshToken> deleteByUserId(Long userId) {
        QRefreshToken qRefreshToken = QRefreshToken.refreshToken;
        RefreshToken refreshToken = queryFactory.selectFrom(qRefreshToken)
                .where(qRefreshToken.userId.eq(userId))
                .fetchOne();
        if (refreshToken != null) {
            entityManager.remove(refreshToken);
        }
        return Optional.ofNullable(refreshToken);
    }
}
