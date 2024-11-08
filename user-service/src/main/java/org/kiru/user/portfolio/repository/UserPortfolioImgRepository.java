package org.kiru.user.portfolio.repository;

import java.util.List;
import org.kiru.core.userPortfolioImg.entity.UserPortfolioImg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPortfolioImgRepository extends JpaRepository<UserPortfolioImg, Long> {
    List<UserPortfolioImg> findByUserId(Long userId);
}