package org.kiru.user.userReport.repository;

import org.kiru.core.user.userReport.domain.UserReport;
import org.kiru.core.user.userReport.entity.UserReportJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserReportJpaRepository extends JpaRepository<UserReportJpaEntity, Long> {
    Optional<UserReportJpaEntity> findByUserIdAndReportedId(Long userId, Long reportedId);
    UserReportJpaEntity save(UserReport userReport);
}
