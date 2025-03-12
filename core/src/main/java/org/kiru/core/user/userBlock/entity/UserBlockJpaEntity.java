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
@Table(name = "user_block")
public class UserBlockJpaEntity extends BaseTimeEntity implements UserBlock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // 차단을 요청한 유저 ID
    @NotNull
    @Column(name = "blocker_id")
    private Long blockerId;
    //  차단된 유저 ID
    @NotNull
    @Column(name = "blocked_id")
    private Long blockedId;
}
