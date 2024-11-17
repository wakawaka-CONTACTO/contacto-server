package org.kiru.user.user.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.kiru.core.user.userPortfolioImg.entity.UserPortfolioImg;
import org.kiru.user.portfolio.dto.res.UserPortfolioResDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserPortfolioRepository extends JpaRepository<UserPortfolioImg, Long> {
    List<UserPortfolioImg> findAllByUserId(Long userId);

    void deleteAllByUserId(Long userId);
    Optional<UserPortfolioImg> findByUserIdAndSequence(Long userId, int sequence);

//    @Query("SELECT new org.kiru.user.portfolio.dto.res.UserPortfolioResDto(u.id, u.username, p.portfolioId, p.portfolioImageUrl) " +
//            "FROM UserPortfolioImg p JOIN UserJpaEntity u ON p.userId = u.id WHERE p.userId IN :userIds")
//    List<UserPortfolioResDto> findAllPortfoliosByUserIds(List<Long> userIds);

    @Query("SELECT new org.kiru.user.portfolio.dto.res.UserPortfolioResDto(" +
            "u.id, u.username, upi.portfolioId, upi.portfolioImageUrl) " +
            "FROM UserPortfolioImg upi " +
            "JOIN UserJpaEntity u ON upi.userId = u.id " +
            "WHERE upi.userId IN :userIds")
    List<UserPortfolioResDto> findAllPortfoliosByUserIds(List<Long> userIds);

    List<UserPortfolioImg> findAllByUserIdIn(List<Long> allParticipantIds);

    @Query("SELECT upi FROM UserPortfolioImg upi " +
            "WHERE upi.userId IN :userIds " +
            "AND upi.sequence = (SELECT MIN(upi2.sequence) FROM UserPortfolioImg upi2 WHERE upi2.userId = upi.userId)")
    List<UserPortfolioImg> findAllByUserIdInWithMinSequence(List<Long> userIds);
}
