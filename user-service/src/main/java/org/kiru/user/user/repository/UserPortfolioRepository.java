package org.kiru.user.user.repository;

import java.util.List;
import java.util.Optional;
import org.kiru.core.user.userPortfolioImg.entity.UserPortfolioImg;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPortfolioRepository extends JpaRepository<UserPortfolioImg, Long> {
    List<UserPortfolioImg> findAllByUserId(Long userId);

    void deleteAllByUserId(Long userId);
    Optional<UserPortfolioImg> findByUserIdAndSequence(Long userId, int sequence);
}
