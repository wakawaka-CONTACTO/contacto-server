package org.kiru.user.auth.jwt.refreshtoken.repository;

import java.util.Optional;
import org.kiru.core.user.refreshtoken.RefreshToken;

public interface RefreshTokenRepositoryCustom  {
    Optional<RefreshToken> deleteByUserId(Long userId);
}
