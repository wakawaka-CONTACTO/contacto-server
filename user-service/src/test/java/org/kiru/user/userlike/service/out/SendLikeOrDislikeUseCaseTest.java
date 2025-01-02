package org.kiru.user.userlike.service.out;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kiru.core.user.userlike.domain.LikeStatus;
import org.kiru.core.user.userlike.domain.UserLike;
import org.kiru.core.user.userlike.entity.UserLikeJpaEntity;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SendLikeOrDislikeUseCaseTest {

    @Mock
    private SendLikeOrDislikeUseCase sendLikeOrDislikeUseCase;

    private UserLikeJpaEntity testUserLike;

    @BeforeEach
    void setUp() {
        testUserLike = UserLikeJpaEntity.builder()
            .userId(1L)
            .likedUserId(2L)
            .likeStatus(LikeStatus.LIKE)
            .isMatched(false)
            .build();
    }

    @Test
    @DisplayName("좋아요 보내기 - 성공")
    void sendOrDislike_Like_Success() {
        // Given
        when(sendLikeOrDislikeUseCase.sendOrDislike(1L, 2L, LikeStatus.LIKE))
            .thenReturn(testUserLike);

        // When
        UserLike result = sendLikeOrDislikeUseCase.sendOrDislike(1L, 2L, LikeStatus.LIKE);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getLikedUserId()).isEqualTo(2L);
        assertThat(result.getLikeStatus()).isEqualTo(LikeStatus.LIKE);
        assertThat(result.isMatched()).isFalse();
    }

    @Test
    @DisplayName("싫어요 보내기 - 성공")
    void sendOrDislike_Dislike_Success() {
        // Given
        UserLikeJpaEntity dislikeUserLike = UserLikeJpaEntity.builder()
            .userId(1L)
            .likedUserId(2L)
            .likeStatus(LikeStatus.DISLIKE)
            .isMatched(false)
            .build();

        when(sendLikeOrDislikeUseCase.sendOrDislike(1L, 2L, LikeStatus.DISLIKE))
            .thenReturn(dislikeUserLike);

        // When
        UserLike result = sendLikeOrDislikeUseCase.sendOrDislike(1L, 2L, LikeStatus.DISLIKE);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getLikedUserId()).isEqualTo(2L);
        assertThat(result.getLikeStatus()).isEqualTo(LikeStatus.DISLIKE);
        assertThat(result.isMatched()).isFalse();
    }

    @Test
    @DisplayName("매칭된 좋아요 - 성공")
    void sendOrDislike_Matched_Success() {
        // Given
        UserLike matchedUserLike = UserLikeJpaEntity.builder()
            .userId(1L)
            .likedUserId(2L)
            .likeStatus(LikeStatus.LIKE)
            .isMatched(true)
            .build();

        when(sendLikeOrDislikeUseCase.sendOrDislike(1L, 2L, LikeStatus.LIKE))
            .thenReturn(matchedUserLike);

        // When
        UserLike result = sendLikeOrDislikeUseCase.sendOrDislike(1L, 2L, LikeStatus.LIKE);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getLikedUserId()).isEqualTo(2L);
        assertThat(result.getLikeStatus()).isEqualTo(LikeStatus.LIKE);
        assertThat(result.isMatched()).isTrue();
    }
}
