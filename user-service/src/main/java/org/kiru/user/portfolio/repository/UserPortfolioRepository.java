package org.kiru.user.portfolio.repository;

import jakarta.persistence.QueryHint;
import java.util.List;
import org.kiru.core.user.userPortfolioItem.entity.UserPortfolioImg;
import org.kiru.user.portfolio.adapter.dto.UserPortfolioProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

@Repository
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
    List<UserPortfolioImg> findAllByUserIdInWithItemUrlMinSequence(List<Long> userIds);

    @Query("""
    SELECT 
        upi.portfolioId as portfolioId,
        upi.userId as userId,
        upi.userName as username,
        STRING_AGG(upi.portfolioImageUrl,',') as portfolioImageUrl
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
    List<UserPortfolioProjection> findAllPortfoliosByUserIds(List<Long> userIds);
}
