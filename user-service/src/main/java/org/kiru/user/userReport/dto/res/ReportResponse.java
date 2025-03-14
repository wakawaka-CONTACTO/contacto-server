package org.kiru.user.userReport.dto.res;

import lombok.Builder;
import lombok.Getter;
import org.kiru.core.user.userReport.domain.ReportReason;
import org.kiru.core.user.userReport.domain.ReportStatus;
import org.kiru.core.user.userReport.domain.UserReport;

@Builder
@Getter
public class ReportResponse {
    Long userId;
    Long reportedUserId;
    ReportReason reportReason;
    ReportStatus reportStatus;

    public static ReportResponse of(UserReport userReport) {
        return ReportResponse.builder()
                .userId(userReport.getUserId())
                .reportedUserId(userReport.getReportedUserId())
                .reportReason(userReport.getReportReason())
                .reportStatus(userReport.getReportStatus())
                .build();
    }
}
