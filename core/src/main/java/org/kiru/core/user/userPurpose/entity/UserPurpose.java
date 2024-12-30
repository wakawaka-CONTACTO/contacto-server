package org.kiru.core.user.userPurpose.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.kiru.core.user.userPurpose.domain.PurposeType;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(
        name = "user_purposes",
        indexes = {
                @Index(name = "idx_purpose_user", columnList = "purpose_type, user_id"),
                @Index(name = "idx_user_id", columnList = "user_id")  // 기존 유지
        }
)@Getter
public class UserPurpose {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "user_id")
    private Long userId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "purpose_type")
    private PurposeType purposeType;
}
