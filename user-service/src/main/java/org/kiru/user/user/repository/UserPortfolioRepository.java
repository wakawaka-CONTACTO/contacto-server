package org.kiru.user.user.repository;

import java.util.List;
import org.kiru.core.userPortfolioImg.domain.UserPortfolio;
import org.kiru.core.userPortfolioImg.entity.UserPortfolioImg;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPortfolioRepository extends JpaRepository<UserPortfolioImg, Long> {
    List<UserPortfolioImg> findAllByUserId(Long userId);
}
