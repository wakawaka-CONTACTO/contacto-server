package org.kiru.user.user.repository;

import java.util.List;
import java.util.Optional;
import org.hibernate.annotations.Cache;
import org.kiru.core.user.user.entity.UserJpaEntity;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserJpaEntity,Long> {
    Optional<UserJpaEntity> findByIdAndSocialId(Long userId, String socialID);

    Optional<UserJpaEntity> findByEmail(String email);

    @Query("SELECT u.id, u.username " +
            "FROM UserJpaEntity u " +
            "WHERE u.id IN :userIds")
    List<Object[]> findUsernamesByIds(List<Long> userIds);

    @Query("SELECT u.email " +
            "FROM UserJpaEntity u " +
            "WHERE u.username = :username")
    Optional<String> findByUsername(String username);
}
