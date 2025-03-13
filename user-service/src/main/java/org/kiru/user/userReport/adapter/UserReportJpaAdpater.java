package org.kiru.user.userReport.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kiru.core.user.userReport.domain.ReportReason;
import org.kiru.core.user.userReport.domain.ReportStatus;
import org.kiru.core.user.userReport.domain.UserReport;
import org.kiru.core.user.userReport.entity.UserReportJpaEntity;
import org.kiru.user.userReport.repository.UserReportJpaRepository;
import org.kiru.user.userReport.service.out.ReportUserQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Repository
@Transactional(readOnly = true)
public class UserReportJpaAdpater implements ReportUserQuery {
    private final UserReportJpaRepository userReportRepository;

    @Transactional
    @Override
    public UserReport reportUser(Long userId, Long reportedId, int reportReasonIdx) {
        ReportReason reportReason = ReportReason.fromIndex(reportReasonIdx);
        log.info("Reporting user {} for user {} with report reason {}", reportedId, userId, reportReason);
        UserReport userReport = userReportRepository.findByUserIdAndReportedId(userId, reportedId)
                .orElseGet(() -> UserReportJpaEntity.of(userId, reportedId, reportReason, ReportStatus.PENDING));
        userReportRepository.save(userReport);
        return userReport;
    }
}
