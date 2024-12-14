package org.kiru.user.portfolio.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kiru.user.portfolio.dto.res.UserPortfolioResDto;
import org.kiru.user.portfolio.service.out.GetUserPortfoliosQuery;
import org.kiru.user.user.repository.UserPurposeRepository;
import org.kiru.user.userlike.repository.UserLikeRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {

    @InjectMocks
    private PortfolioService portfolioService;

    @Mock
    private UserPurposeRepository userPurposeRepository;

    @Mock
    private GetUserPortfoliosQuery getUserPortfoliosQuery;

    @Mock
    private UserLikeRepository userLikeRepository;

    private Long testUserId;
    private Pageable pageable;
    private List<UserPortfolioResDto> testPortfolios;

    @BeforeEach
    void setUp() {
        testUserId = 1L;
        pageable = PageRequest.of(0, 10);
        
        testPortfolios = Arrays.asList(
            UserPortfolioResDto.of(1L, 1L, "Description 1", List.of("url1", "url2")),
            UserPortfolioResDto.of(2L, 2L, "Description 2", List.of("url3", "url4"))
        );

        // Mock Slice 객체 생성
        Slice<Long> mockSlice = new SliceImpl<>(Arrays.asList(1L, 2L));
        when(userPurposeRepository.findUserIdsByPurposeTypesOrderByCount(any(), any()))
            .thenReturn(mockSlice);
    }

    @Test
    @DisplayName("사용자 포트폴리오 조회 - 성공")
    void getUserPortfolios_Success() {
        // Given
        List<Long> matchedUserIds = Arrays.asList(2L, 3L);
        List<Long> matchingUserIds = Arrays.asList(4L, 5L);
        Slice<Long> likedUserIds = new SliceImpl<>(Arrays.asList(6L, 7L));
        Slice<Long> popularUserIds = new SliceImpl<>(Arrays.asList(8L, 9L));

        when(userLikeRepository.findAllMatchedUserIdByUserId(testUserId))
            .thenReturn(matchedUserIds);
        when(userPurposeRepository.findUserIdsByPurposeTypesOrderByCount(any(), any()))
            .thenReturn(new SliceImpl<>(matchingUserIds));
        when(userLikeRepository.findAllLikeMeUserIdAndNotMatchedByLikedUserId(any(), any()))
            .thenReturn(likedUserIds);
        when(userLikeRepository.findAllUserIdOrderByLikedUserIdCountDesc(any()))
            .thenReturn(popularUserIds);
        when(getUserPortfoliosQuery.findAllPortfoliosByUserIds(anyList()))
            .thenReturn(testPortfolios);

        // When
        List<UserPortfolioResDto> result = portfolioService.getUserPortfolios(testUserId, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        verify(userLikeRepository).findAllMatchedUserIdByUserId(testUserId);
        verify(userPurposeRepository).findUserIdsByPurposeTypesOrderByCount(any(), any());
    }

    @Test
    @DisplayName("사용자 포트폴리오 조회 - 빈 결과")
    void getUserPortfolios_EmptyResult() {
        // Given
        when(userLikeRepository.findAllMatchedUserIdByUserId(testUserId))
            .thenReturn(List.of());
        when(userPurposeRepository.findUserIdsByPurposeTypesOrderByCount(any(), any()))
            .thenReturn(new SliceImpl<>(List.of()));
        when(userLikeRepository.findAllLikeMeUserIdAndNotMatchedByLikedUserId(any(), any()))
            .thenReturn(new SliceImpl<>(List.of()));
        when(userLikeRepository.findAllUserIdOrderByLikedUserIdCountDesc(any()))
            .thenReturn(new SliceImpl<>(List.of()));
        when(getUserPortfoliosQuery.findAllPortfoliosByUserIds(anyList()))
            .thenReturn(List.of());

        // When
        List<UserPortfolioResDto> result = portfolioService.getUserPortfolios(testUserId, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("사용자 포트폴리오 조회 - 중복 제거 확인")
    void getUserPortfolios_DuplicateRemoval() {
        // Given
        List<Long> matchedUserIds = Arrays.asList(2L, 3L);
        Slice<Long> likedUserIds = new SliceImpl<>(Arrays.asList(3L, 4L, 5L)); // 중복된 ID 포함
        Slice<Long> popularUserIds = new SliceImpl<>(Arrays.asList(4L, 5L, 6L)); // 중복된 ID 포함

        when(userLikeRepository.findAllMatchedUserIdByUserId(testUserId))
            .thenReturn(matchedUserIds);
        when(userLikeRepository.findAllLikeMeUserIdAndNotMatchedByLikedUserId(any(), any()))
            .thenReturn(likedUserIds);
        when(userLikeRepository.findAllUserIdOrderByLikedUserIdCountDesc(any()))
            .thenReturn(popularUserIds);
        when(getUserPortfoliosQuery.findAllPortfoliosByUserIds(anyList()))
            .thenReturn(testPortfolios);
        // When
        List<UserPortfolioResDto> result = portfolioService.getUserPortfolios(testUserId, pageable);
        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
    }
}
