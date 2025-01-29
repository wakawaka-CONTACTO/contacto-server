package org.kiru.user.auth.jwt.refreshtoken.repository;

import java.util.Optional;
import org.kiru.core.user.refreshtoken.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String>, RefreshTokenRepositoryCustom {
    Optional<RefreshToken> findByUserId(Long userId);
    void deleteRefreshTokenByUserId(final Long userId);
}