package org.kiru.user.portfolio.repository;

import jakarta.persistence.QueryHint;
import java.util.List;
import org.kiru.core.user.userPortfolioItem.entity.UserPortfolioImg;
import org.kiru.user.portfolio.adapter.dto.UserPortfolioProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
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

    @Query(value = """
    SELECT 
        MIN(upi.portfolio_id) AS portfolioId,
        upi.user_id AS userId,
        upi.username AS username,
        STRING_AGG(upi.portfolio_image_url, ',' ORDER BY upi.sequence) AS portfolioImageUrl
    FROM 
        user_portfolio_imgs upi
    WHERE 
        upi.user_id IN (:userIds)
    GROUP BY 
        upi.user_id, upi.username
    """, nativeQuery = true)
    @QueryHints(value = {
            @QueryHint(name = "org.hibernate.readOnly", value = "true"),
            @QueryHint(name = "org.hibernate.fetchSize", value = "150"),
            @QueryHint(name = "jakarta.persistence.query.timeout", value = "5000")

    })
    List<UserPortfolioProjection> findAllPortfoliosByUserIds(@Param("userIds") List<Long> userIds);
}
