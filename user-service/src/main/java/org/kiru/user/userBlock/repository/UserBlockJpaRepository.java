package org.kiru.user.userBlock.repository;

import jakarta.persistence.QueryHint;
import org.kiru.core.user.userBlock.domain.UserBlock;
import org.kiru.core.user.userBlock.entity.UserBlockJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBlockJpaRepository extends JpaRepository<UserBlockJpaEntity, Long> {
    Optional<UserBlockJpaEntity> findByUserIdAndBlockedUserId(Long userId, Long blockedUserId);

    @Query("SELECT ub.blockedUserId FROM UserBlockJpaEntity ub WHERE ub.userId = :userId")
    @QueryHints(value = {
            @QueryHint(name = "org.hibernate.readOnly", value = "true"),
            @QueryHint(name = "org.hibernate.fetchSize", value = "150"),
            @QueryHint(name = "jakarta.persistence.query.timeout", value = "5000")
    })
    List<Long> findAllBlockedUserIdByUserId(@Param("userId") Long userId);

    @Query("""
    SELECT CASE
        WHEN ub.userId = :userId THEN ub.blockedUserId
        ELSE ub.userId
    END
    FROM UserBlockJpaEntity ub
    WHERE ub.userId = :userId OR ub.blockedUserId = :userId
""")
    @QueryHints(value = {
            @QueryHint(name = "org.hibernate.readOnly", value = "true"),
            @QueryHint(name = "org.hibernate.fetchSize", value = "150"),
            @QueryHint(name = "jakarta.persistence.query.timeout", value = "5000")
    })
    List<Long> findAllBlockedOrBlockingUserByUserIds(@Param("userId") Long userId);
}
