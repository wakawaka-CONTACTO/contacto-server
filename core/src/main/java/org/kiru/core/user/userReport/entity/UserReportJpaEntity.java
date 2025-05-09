package org.kiru.core.user.userReport.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.kiru.core.user.common.BaseTimeEntity;
import org.kiru.core.user.userReport.domain.ReportReason;
import org.kiru.core.user.userReport.domain.ReportStatus;
import org.kiru.core.user.userReport.domain.UserReport;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user_report",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "reported_user_id"}))
public class UserReportJpaEntity extends BaseTimeEntity implements UserReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // 신고를 요청한 유저 ID
    @NotNull
    @Column(name = "user_id")
    private Long userId;
    //  신고를 당한 유저 ID
    @NotNull
    @Column(name = "reported_user_id")
    private Long reportedUserId;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "report_reason")
    private ReportReason reportReason;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "report_status")
    private ReportStatus reportStatus;

    public static UserReportJpaEntity of(Long userId, Long reportedUserId,
                                         ReportReason reportReason, ReportStatus reportStatus) {
        return UserReportJpaEntity.builder()
                .userId(userId)
                .reportedUserId(reportedUserId)
                .reportReason(reportReason)
                .reportStatus(reportStatus)
                .build();
    }
}
