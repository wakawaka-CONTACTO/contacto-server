package org.kiru.core.user.userReport.domain;

import java.time.LocalDateTime;

public interface UserReport {
    Long getUserId();
    Long getReportedId();
    ReportReason getReportReason();
    ReportStatus getReportStatus();
    LocalDateTime getCreatedAt();
}
