package org.kiru.core.user.userReport.domain;

import java.time.LocalDateTime;

public interface UserReport {
    Long getUserId();
    Long getReportedId();
    LocalDateTime getCreatedAt();
}
