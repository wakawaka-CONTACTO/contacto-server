package org.kiru.user.userReport.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kiru.core.exception.BadRequestException;
import org.kiru.core.exception.code.FailureCode;
import org.kiru.user.auth.argumentresolve.UserId;
import org.kiru.user.userReport.dto.req.ReportRequest;
import org.kiru.user.userReport.dto.res.ReportResponse;
import org.kiru.user.userReport.service.UserReportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/users/reports")
@RequiredArgsConstructor
public class UserReportController {
    private final UserReportService userReportService;

    @PostMapping
    public ResponseEntity<ReportResponse> reportUser(
            @UserId Long userId,
            @RequestBody ReportRequest reportRequest) {
        log.info("Reporting user {} for user {}", reportRequest.reportedUserId(), userId);
        if(userId.equals(reportRequest.reportedUserId())) {
            throw new BadRequestException(FailureCode.INVALID_USER_REPORT);
        }
        ReportResponse reportResponse = userReportService.reportUser(userId, reportRequest.reportedUserId(), reportRequest.reportReasonIdx());
        return ResponseEntity.status(HttpStatus.CREATED).body(reportResponse);
    }
}
