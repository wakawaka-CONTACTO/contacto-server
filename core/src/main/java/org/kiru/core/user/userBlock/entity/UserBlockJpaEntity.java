package org.kiru.core.user.userBlock.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.kiru.core.user.common.BaseTimeEntity;
import org.kiru.core.user.userBlock.domain.UserBlock;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user_block",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "blocked_user_id"}))
public class UserBlockJpaEntity extends BaseTimeEntity implements UserBlock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // 차단을 요청한 유저 ID
    @NotNull
    @Column(name = "user_id")
    private Long userId;
    //  차단된 유저 ID
    @NotNull
    @Column(name = "blocked_user_id")
    private Long blockedUserId;

    public static UserBlockJpaEntity of(Long userId, Long blockedUserId) {
        return UserBlockJpaEntity.builder()
                .userId(userId)
                .blockedUserId(blockedUserId)
                .build();
    }
}
