package org.kiru.user.userReport.service;

import lombok.extern.slf4j.Slf4j;
import org.kiru.core.user.userReport.domain.UserReport;
import org.kiru.user.userReport.dto.res.ReportResponse;
import org.kiru.user.userReport.service.out.ReportUserQuery;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserReportService {
    private final ReportUserQuery reportUserQuery;
    public UserReportService(ReportUserQuery reportUserQuery) { this.reportUserQuery = reportUserQuery; }
    public ReportResponse reportUser(Long userId, Long reportedId, int reportReasonIdx) {
        UserReport userReport = reportUserQuery.reportUser(userId, reportedId, reportReasonIdx);
        return ReportResponse.of(userReport);
    }
}
