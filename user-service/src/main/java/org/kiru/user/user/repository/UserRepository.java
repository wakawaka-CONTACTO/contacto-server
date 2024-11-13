package org.kiru.user.user.repository;

import java.util.Optional;
import org.kiru.core.user.user.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserJpaEntity,Long> {
    Optional<UserJpaEntity> findByIdAndSocialId(Long userId, String socialID);

    Optional<UserJpaEntity> findByEmail(String email);
}
