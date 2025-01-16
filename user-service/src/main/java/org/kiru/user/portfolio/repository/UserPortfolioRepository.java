package org.kiru.user.portfolio.repository;

import jakarta.persistence.QueryHint;
import java.util.List;
import org.kiru.core.user.userPortfolioImg.entity.UserPortfolioImg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

public interface UserPortfolioRepository extends JpaRepository<UserPortfolioImg, Long> {
    @Query("SELECT upi FROM UserPortfolioImg upi WHERE upi.userId = :userId ORDER BY upi.sequence LIMIT 10")
    List<UserPortfolioImg> findAllByUserId(Long userId);

    void deleteAllByUserId(Long userId);

    @Query("SELECT upi FROM UserPortfolioImg upi " +
            "WHERE upi.userId IN :userIds " +
            "AND upi.sequence = 1")
    @QueryHints(value = {
            @QueryHint(name = "org.hibernate.readOnly", value = "true"),
            @QueryHint(name = "org.hibernate.fetchSize", value = "150"),
            @QueryHint(name = "jakarta.persistence.query.timeout", value = "5000")

    })
    List<UserPortfolioImg> findAllByUserIdInWithMinSequence(List<Long> userIds);

    @Query("""
    SELECT 
        upi.portfolioId,
        upi.userId,
        upi.userName,
        STRING_AGG(upi.portfolioImageUrl,',')
    FROM 
       UserPortfolioImg upi
    WHERE 
        upi.userId IN :userIds
    GROUP BY 
         upi.userId, upi.userName, upi.portfolioId
    """)
    @QueryHints(value = {
            @QueryHint(name = "org.hibernate.readOnly", value = "true"),
            @QueryHint(name = "org.hibernate.fetchSize", value = "150"),
            @QueryHint(name = "jakarta.persistence.query.timeout", value = "5000")

    })
    List<Object[]> findAllPortfoliosByUserIds(List<Long> userIds);
}
