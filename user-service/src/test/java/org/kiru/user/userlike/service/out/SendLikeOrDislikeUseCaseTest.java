package org.kiru.user.userlike.service.out;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kiru.core.user.userBlock.entity.UserBlockJpaEntity;
import org.kiru.core.user.userlike.domain.LikeStatus;
import org.kiru.core.user.userlike.domain.UserLike;
import org.kiru.core.user.userlike.entity.UserLikeJpaEntity;
import org.kiru.user.userlike.adapter.UserLikeJpaAdapter;
import org.kiru.user.userlike.repository.UserLikeJpaRepository;
import org.kiru.user.userBlock.repository.UserBlockJpaRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SendLikeOrDislikeUseCaseTest {

    @InjectMocks
    private UserLikeJpaAdapter sendLikeOrDislikeUseCase;

    @Mock
    private UserLikeJpaRepository userLikeRepository;

    @Mock
    private UserBlockJpaRepository userBlockRepository;

    private UserLikeJpaEntity existingLikeFromOtherUser; // 상대방의 기록
    private UserLikeJpaEntity userLikeJpaEntity; // 현재 유저의 기록

    @Test
    @DisplayName("상대방이 나를 좋아한 상태에서 내가 좋아요를 누른 경우")
    void whenOtherUserLikedMeAndILikeThem_ThenMatchIsCreated() {
        // given
        existingLikeFromOtherUser =  UserLikeJpaEntity.of(2L, 1L, LikeStatus.LIKE, false);
        userLikeJpaEntity = UserLikeJpaEntity.of(1L, 2L, LikeStatus.DISLIKE, false);

        when(userLikeRepository.findByUserIdAndLikedUserId(1L, 2L))
                .thenReturn(Optional.of(userLikeJpaEntity));
        when(userBlockRepository.findByUserIdAndBlockedUserId(2L, 1L))
                .thenReturn(Optional.empty());
        when(userLikeRepository.findOppositeLike(2L, 1L, LikeStatus.LIKE))
                .thenReturn(existingLikeFromOtherUser);
        // when
        UserLike result = sendLikeOrDislikeUseCase.sendLikeOrDislike(1L, 2L, LikeStatus.LIKE);
        // then
        AssertionsForClassTypes.assertThat(result.isMatched()).isTrue();
        AssertionsForClassTypes.assertThat(result.getLikeStatus()).isEqualTo(LikeStatus.LIKE);
        verify(userLikeRepository, times(1)).save(result);
        verify(userLikeRepository, times(1)).save((UserLike) existingLikeFromOtherUser);
    }

    @Test
    @DisplayName("상대방이 나를 차단한 경우")
    void whenOtherUserBlockedMe_ThenNoMatch() {
        // given
        userLikeJpaEntity = UserLikeJpaEntity.of(1L, 2L, LikeStatus.DISLIKE, false);
        when(userLikeRepository.findByUserIdAndLikedUserId(1L, 2L))
                .thenReturn(Optional.of(userLikeJpaEntity));
        when(userBlockRepository.findByUserIdAndBlockedUserId(2L, 1L))
                .thenReturn(Optional.of(new UserBlockJpaEntity()));

        // when
        UserLike result = sendLikeOrDislikeUseCase.sendLikeOrDislike(1L, 2L, LikeStatus.LIKE);

        // then
        AssertionsForClassTypes.assertThat(result.isMatched()).isFalse();
        AssertionsForClassTypes.assertThat(result.getLikeStatus()).isEqualTo(LikeStatus.LIKE);
        verify(userLikeRepository, times(1)).save(result);
        verify(userLikeRepository, never()).save((UserLike) existingLikeFromOtherUser);
    }

    @Test
    @DisplayName("이미 매칭된 상태에서 다시 좋아요를 누른 경우")
    void whenAlreadyMatched_ThenKeepMatch() {
        // given
        userLikeJpaEntity = UserLikeJpaEntity.of(1L, 2L, LikeStatus.LIKE, true);
        when(userLikeRepository.findByUserIdAndLikedUserId(1L, 2L))
                .thenReturn(Optional.of(userLikeJpaEntity));
        when(userBlockRepository.findByUserIdAndBlockedUserId(2L, 1L))
                .thenReturn(Optional.empty());

        // when
        UserLike result = sendLikeOrDislikeUseCase.sendLikeOrDislike(1L, 2L, LikeStatus.LIKE);

        // then
        AssertionsForClassTypes.assertThat(result.isMatched()).isTrue();
        AssertionsForClassTypes.assertThat(result.getLikeStatus()).isEqualTo(LikeStatus.LIKE);
        verify(userLikeRepository, times(1)).findByUserIdAndLikedUserId(1L, 2L);
        verify(userBlockRepository, times(1)).findByUserIdAndBlockedUserId(2L, 1L);
    }

    @Test
    @DisplayName("상대방이 나를 좋아하지 않은 상태에서 내가 좋아요를 누른 경우")
    void whenOtherUserDidNotLikeMeAndILikeThem_ThenNoMatch() {
        // given
        userLikeJpaEntity = UserLikeJpaEntity.of(1L, 2L, LikeStatus.DISLIKE, false);

        when(userLikeRepository.findByUserIdAndLikedUserId(1L, 2L))
                .thenReturn(Optional.of(userLikeJpaEntity));
        when(userLikeRepository.findOppositeLike(2L, 1L, LikeStatus.LIKE))
                .thenReturn(null);

        // when
        UserLike result = sendLikeOrDislikeUseCase.sendLikeOrDislike(1L, 2L, LikeStatus.LIKE);

        // then
        AssertionsForClassTypes.assertThat(result.isMatched()).isFalse();
        AssertionsForClassTypes.assertThat(result.getLikeStatus()).isEqualTo(LikeStatus.LIKE);
        verify(userLikeRepository, times(1)).save(result);
    }

    @Test
    @DisplayName("상대방이 나를 좋아한 상태에서 내가 싫어요를 누른 경우")
    void whenOtherUserLikedMeAndIDislikeThem_ThenNoMatch() {
        // given
        existingLikeFromOtherUser = UserLikeJpaEntity.of(2L, 1L, LikeStatus.LIKE, false);
        userLikeJpaEntity = UserLikeJpaEntity.of(1L, 2L, LikeStatus.DISLIKE, false);

        when(userLikeRepository.findByUserIdAndLikedUserId(1L, 2L))
                .thenReturn(Optional.of(userLikeJpaEntity));
        when(userLikeRepository.findOppositeLike(2L, 1L, LikeStatus.LIKE))
                .thenReturn(existingLikeFromOtherUser);

        // when
        UserLike result = sendLikeOrDislikeUseCase.sendLikeOrDislike(1L, 2L, LikeStatus.DISLIKE);

        // then
        AssertionsForClassTypes.assertThat(result.isMatched()).isFalse();
        AssertionsForClassTypes.assertThat(result.getLikeStatus()).isEqualTo(LikeStatus.DISLIKE);
        verify(userLikeRepository, times(1)).save(result);
        verify(userLikeRepository, never()).save((UserLike) existingLikeFromOtherUser);
    }

    @Test
    @DisplayName("서로 좋아하지 않은 경우")
    void whenNeitherUserLikesEachOther_ThenNoMatch() {
        // given
        userLikeJpaEntity = UserLikeJpaEntity.of(1L, 2L, LikeStatus.DISLIKE, false);
        existingLikeFromOtherUser = UserLikeJpaEntity.of(2L, 1L, LikeStatus.DISLIKE, false);
        when(userLikeRepository.findByUserIdAndLikedUserId(1L, 2L))
                .thenReturn(Optional.of(userLikeJpaEntity));
        when(userLikeRepository.findOppositeLike(2L, 1L, LikeStatus.LIKE))
                .thenReturn(existingLikeFromOtherUser);

        // when
        UserLike result = sendLikeOrDislikeUseCase.sendLikeOrDislike(1L, 2L, LikeStatus.DISLIKE);

        // then
        AssertionsForClassTypes.assertThat(result.isMatched()).isFalse();
        AssertionsForClassTypes.assertThat(result.getLikeStatus()).isEqualTo(LikeStatus.DISLIKE);
        verify(userLikeRepository, times(1)).save(result);
    }
}
