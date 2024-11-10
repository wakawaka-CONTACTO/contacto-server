package org.kiru.core.user.userPurpose.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "user_purposes")
@Getter
public class UserPurpose {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long userId;

    @NotNull
    @Enumerated(EnumType.STRING)
    private PurposeType purposeType;
}
