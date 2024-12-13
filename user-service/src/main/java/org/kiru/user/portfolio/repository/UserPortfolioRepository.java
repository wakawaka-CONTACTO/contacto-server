package org.kiru.user.portfolio.repository;

import java.util.List;
import org.kiru.core.user.userPortfolioImg.entity.UserPortfolioImg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserPortfolioRepository extends JpaRepository<UserPortfolioImg, Long> {

    @Query("SELECT upi FROM UserPortfolioImg upi WHERE upi.userId = :userId ORDER BY upi.sequence LIMIT 10")
    List<UserPortfolioImg> findAllByUserId(Long userId);

    void deleteAllByUserId(Long userId);

    @Query("SELECT upi FROM UserPortfolioImg upi " +
            "WHERE upi.userId IN :userIds " +
            "AND upi.sequence = 1")
    List<UserPortfolioImg> findAllByUserIdInWithMinSequence(List<Long> userIds);
}
