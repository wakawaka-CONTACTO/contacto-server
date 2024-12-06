package org.kiru.user.user.repository;

import java.util.List;
import java.util.Optional;
import org.kiru.core.user.userPortfolioImg.entity.UserPortfolioImg;
import org.kiru.user.portfolio.dto.res.UserPortfolioResDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserPortfolioRepository extends JpaRepository<UserPortfolioImg, Long> {

    @Query("SELECT upi FROM UserPortfolioImg upi WHERE upi.userId = :userId ORDER BY upi.sequence LIMIT 10")
    List<UserPortfolioImg> findAllByUserId(Long userId);

    void deleteAllByUserId(Long userId);
    Optional<UserPortfolioImg> findByUserIdAndSequence(Long userId, int sequence);


    @Query("SELECT new org.kiru.user.portfolio.dto.res.UserPortfolioResDto(" +
            "u.id, u.username, upi.portfolioId, upi.portfolioImageUrl) " +
            "FROM UserPortfolioImg upi " +
            "JOIN UserJpaEntity u ON upi.userId = u.id " +
            "WHERE upi.userId IN :userIds")
    List<UserPortfolioResDto> findAllPortfoliosByUserIds(List<Long> userIds);

    List<UserPortfolioImg> findAllByUserIdIn(List<Long> allParticipantIds);
//    SELECT upi
//    FROM user_portfolio_imgs upi
//    WHERE upi.user_id IN (1,2,3,4,5)   AND upi.sequence = 1
//    GROUP BY upi.user_id, upi.id, upi.portfolio_id, upi.portfolio_image_url, upi.sequence
    @Query("SELECT upi FROM UserPortfolioImg upi " +
            "WHERE upi.userId IN :userIds " +
            "AND upi.sequence = 1 " +
            "GROUP BY upi.userId, upi.id, upi.portfolioId, upi.portfolioImageUrl, upi.sequence")
    List<UserPortfolioImg> findAllByUserIdInWithMinSequence(List<Long> userIds);
}
