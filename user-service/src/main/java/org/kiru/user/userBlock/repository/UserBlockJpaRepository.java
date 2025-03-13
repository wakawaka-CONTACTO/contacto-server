package org.kiru.user.userBlock.repository;

import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.QueryHint;
import org.kiru.core.user.userBlock.domain.UserBlock;
import org.kiru.core.user.userBlock.entity.UserBlockJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBlockJpaRepository extends JpaRepository<UserBlockJpaEntity, Long> {
    Optional<UserBlockJpaEntity> findByUserIdAndBlockedId(Long userId, Long blockedId);

    @Query("SELECT ub.blockedId FROM UserBlockJpaEntity ub WHERE ub.userId = :userId")
    @QueryHints(value = {
            @QueryHint(name = "org.hibernate.readOnly", value = "true"),
            @QueryHint(name = "org.hibernate.fetchSize", value = "150"),
            @QueryHint(name = "jakarta.persistence.query.timeout", value = "5000")
    })
    List<Long> findAllBlockedIdByUserId(@Param("userId") Long userId);
    UserBlockJpaEntity save(UserBlock userBlock);
}
