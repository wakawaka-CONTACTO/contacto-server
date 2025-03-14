package org.kiru.user.userReport.service.out;

import org.kiru.core.user.userReport.domain.UserReport;

public interface ReportUserQuery {
    UserReport reportUser(Long userId, Long reportedUserId, int reportReasonIdx);
}
