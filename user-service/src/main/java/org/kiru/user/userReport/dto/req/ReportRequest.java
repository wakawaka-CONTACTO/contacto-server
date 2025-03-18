package org.kiru.user.userReport.dto.req;


public record ReportRequest(
        Long reportedUserId, int reportReasonIdx
) {
}
