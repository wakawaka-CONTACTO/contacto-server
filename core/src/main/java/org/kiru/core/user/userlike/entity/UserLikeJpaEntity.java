package org.kiru.core.user.userlike.entity;

import jakarta.persistence.Column;
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
import org.kiru.core.user.common.BaseTimeEntity;
import org.kiru.core.user.userlike.domain.LikeStatus;
import org.kiru.core.user.userlike.domain.UserLike;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user_like")
public class UserLikeJpaEntity extends BaseTimeEntity implements UserLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    //  좋아요 또는 싫어요를 한사람의 ID
    @NotNull
    @Column(name = "user_id")
    private Long userId;
    //  좋아요 또는 싫어요를 받은 사람의 ID
    @NotNull
    @Column(name = "liked_user_id")
    private Long likedUserId;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "like_status")
    private LikeStatus likeStatus;

    @Column(name = "is_matched")
    @Builder.Default
    private boolean isMatched = false;

    public void setMatched(boolean likeOrDislike) {
        this.isMatched = likeOrDislike;
    }

    public static UserLikeJpaEntity of(Long userId, Long likedUserId, LikeStatus likeStatus, boolean isMatched) {
        return UserLikeJpaEntity.builder()
                .userId(userId)
                .likedUserId(likedUserId)
                .isMatched(isMatched)
                .likeStatus(likeStatus)
                .build();
    }

    public void likeStatus(LikeStatus likeStatus) {
        this.likeStatus = likeStatus;
    }
}
