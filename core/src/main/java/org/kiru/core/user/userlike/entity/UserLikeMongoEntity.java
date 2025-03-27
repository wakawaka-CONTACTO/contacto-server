package org.kiru.core.user.userlike.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.kiru.core.user.userlike.domain.LikeStatus;
import org.kiru.core.user.userlike.domain.UserLike;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Getter
@Document(collection = "user_likes")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLikeMongoEntity implements UserLike {
    @Id
    private String id;

    @Field("user_id")
    private Long userId;

    @Field("liked_user_id")
    private Long likedUserId;

    @Field("like_status")
    private LikeStatus likeStatus;

    @Field("is_matched")
    private boolean isMatched;

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("update_at")
    private LocalDateTime updatedAt;

    public void likeStatus(LikeStatus likeStatus) {
        this.likeStatus = likeStatus;
        this.updatedAt = LocalDateTime.now();
    }

    public void setMatched(boolean likeOrDislike) {
        this.isMatched = likeOrDislike;
        this.updatedAt = LocalDateTime.now();
    }

    public static UserLikeMongoEntity of(Long userId, Long likedUserId, LikeStatus likeStatus, boolean isMatched) {
        LocalDateTime now = LocalDateTime.now();
        return UserLikeMongoEntity.builder()
                .userId(userId)
                .likedUserId(likedUserId)
                .likeStatus(likeStatus)
                .isMatched(isMatched)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void setUpdateAt() { this.updatedAt = LocalDateTime.now(); }
}
