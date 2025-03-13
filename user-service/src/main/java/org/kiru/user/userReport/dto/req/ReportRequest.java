package org.kiru.user.userReport.dto.req;


public record ReportRequest(
        Long reportedId, int reportReasonIdx
) {
}
