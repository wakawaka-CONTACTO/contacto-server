package org.kiru.user.portfolio.repository;

import java.util.List;
import org.kiru.core.user.userPortfolioImg.entity.UserPortfolioImg;
import org.kiru.user.portfolio.dto.res.UserPortfolioResDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPortfolioImgRepository extends JpaRepository<UserPortfolioImg, Long> {

    @Query("SELECT new org.kiru.user.portfolio.dto.res.UserPortfolioResDto(" +
            "u.id, u.username, upi.portfolioId, upi.portfolioImageUrl) " +
            "FROM UserPortfolioImg upi " +
            "JOIN UserJpaEntity u ON upi.userId = u.id " +
            "WHERE upi.userId IN :userIds " +
            "ORDER BY FIELD(upi.userId, :userIds)")
    List<UserPortfolioResDto> findAllPortfoliosByUserIds(List<Long> userIds);

    @Query("SELECT new org.kiru.user.portfolio.dto.res.UserPortfolioResDto(" +
            "u.id, u.username, upi.portfolioId, upi.portfolioImageUrl) " +
            "FROM UserPortfolioImg upi " +
            "JOIN UserJpaEntity u ON upi.userId = u.id " +
            "WHERE upi.userId IN :userIds " +
            "ORDER BY FIELD(upi.userId, :userIds)")
    List<UserPortfolioResDto> findAllPortfoliosGroupedByUserId(List<Long> userIds);
}