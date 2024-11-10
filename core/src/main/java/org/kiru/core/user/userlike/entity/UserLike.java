package org.kiru.core.user.userlike.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.kiru.core.user.common.BaseTimeEntity;
import org.kiru.core.user.userlike.domain.LikeStatus;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLike extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
//  좋아요 또는 싫어요를 한사람의 ID
    @NotNull
    private Long userId;
//  좋아요 또는 싫어요를 받은 사람의 ID
    @NotNull
    private Long likedUserId;

    @Enumerated(EnumType.STRING)
    @NotNull
    private LikeStatus likeStatus;

    private boolean isMatched;

    public boolean isMatched(boolean likeOrDislike) {
        return this.isMatched = likeOrDislike;
    }

    public static UserLike of(Long userId, Long likedUserId, LikeStatus likeStatus, boolean isMatched) {
        return UserLike.builder()
                .userId(userId)
                .likedUserId(likedUserId)
                .isMatched(isMatched)
                .likeStatus(likeStatus)
                .build();
    }
}
