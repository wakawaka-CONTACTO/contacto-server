//package org.kiru.user.portfolio.service;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyList;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.Executor;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.kiru.user.portfolio.dto.res.UserPortfolioResDto;
//import org.kiru.user.portfolio.service.out.GetRecommendUserIdsQuery;
//import org.kiru.user.portfolio.service.out.GetUserPortfoliosQuery;
//import org.kiru.user.user.repository.UserPurposeRepository;
//import org.kiru.user.userlike.dto.Longs;
//import org.kiru.user.userlike.service.out.GetUserLikeQuery;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Slice;
//import org.springframework.data.domain.SliceImpl;
//
//@ExtendWith(MockitoExtension.class)
//class PortfolioServiceTest {
//
//    @InjectMocks
//    private PortfolioService portfolioService;
//
//    @Mock
//    private UserPurposeRepository userPurposeRepository;
//
//    @Mock
//    private GetUserLikeQuery getUserLikeQuery;
//
//    @Mock
//    private GetUserPortfoliosQuery getUserPortfoliosQuery;
//
//    @Mock
//    private GetRecommendUserIdsQuery getRecommendUserIdsQuery;
//
//    @Mock
//    private Executor virtualThreadExecutor;
//
//    private Long testUserId;
//    private Pageable pageable;
//    private List<UserPortfolioResDto> testPortfolios;
//
//    @BeforeEach
//    void setUp() {
//        testUserId = 1L;
//        pageable = PageRequest.of(0, 10);
//
//        testPortfolios = Arrays.asList(
//                UserPortfolioResDto.of(1L, 1L, "Description 1", List.of("url1", "url2")),
//                UserPortfolioResDto.of(2L, 2L, "Description 2", List.of("url3", "url4"))
//        );
//
//        // Mock Slice 객체 생성
//        Slice<Long> mockSlice = new SliceImpl<>(Arrays.asList(1L, 2L));
//        when(userPurposeRepository.findUserIdsByPurposeTypesOrderByCount(any(), any()))
//                .thenReturn(mockSlice);
//    }
//
//    @Test
//    @DisplayName("사용자 포트폴리오 조회 - 성공")
//    void getUserPortfolios_Success() {
//        // Given
//        List<Long> matchedUserIds = Arrays.asList(2L, 3L);
//        List<Long> matchingUserIds = Arrays.asList(4L, 5L);
//        List<Long> likedUserIds = Arrays.asList(6L, 7L);
//        Longs popularUserIds = new Longs(Arrays.asList(8L, 9L));
//        when(getUserLikeQuery.findAllMatchedUserIdByUserId(testUserId))
//                .thenReturn(matchedUserIds);
//        when(userPurposeRepository.findUserIdsByPurposeTypesOrderByCount(any(), any()))
//                .thenReturn(new SliceImpl<>(matchingUserIds));
//        when(getUserLikeQuery.findAllLikeMeUserIdAndNotMatchedByLikedUserId(any(), any()))
//                .thenReturn(likedUserIds);
//        when(getUserLikeQuery.getPopularUserId(any()))
//                .thenReturn(popularUserIds);
//        when(getUserPortfoliosQuery.findAllPortfoliosByUserIds(anyList()))
//                .thenReturn(testPortfolios);
//
//        // When
//        List<UserPortfolioResDto> result = portfolioService.getUserPortfolios(testUserId, pageable);
//
//        // Then
//        assertThat(result).isNotNull();
//        assertThat(result).hasSize(2);
//        verify(getUserLikeQuery).findAllMatchedUserIdByUserId(testUserId);
//        verify(userPurposeRepository).findUserIdsByPurposeTypesOrderByCount(any(), any());
//    }
//
//    @Test
//    @DisplayName("사용자 포트폴리오 조회 - 빈 결과")
//    void getUserPortfolios_EmptyResult() {
//        // Given
//        when(getUserLikeQuery.findAllMatchedUserIdByUserId(testUserId))
//                .thenReturn(List.of());
//        when(userPurposeRepository.findUserIdsByPurposeTypesOrderByCount(any(), any()))
//                .thenReturn(new SliceImpl<>(List.of()));
//        when(getUserLikeQuery.findAllLikeMeUserIdAndNotMatchedByLikedUserId(any(), any()))
//                .thenReturn(List.of());
//        when(getUserPortfoliosQuery.findAllPortfoliosByUserIds(anyList()))
//                .thenReturn(List.of());
//        // When
//        List<UserPortfolioResDto> result = portfolioService.getUserPortfolios(testUserId, pageable);
//        // Then
//        assertThat(result).isNotNull();
//        assertThat(result).isEmpty();
//    }
//
//    @Test
//    @DisplayName("getDistinctUserIds - 이미 매칭된 유저 제외 테스트")
//    void getDistinctUserIds_ExcludeMatchedUsers() {
//        // Given
//        Long testUserId = 1L;
//        Pageable pageable = PageRequest.of(0, 10);
//
//        // 이미 매칭된 유저 ID들
//        List<Long> matchedUserIds = Arrays.asList(2L, 3L);
//
//        // 추천 유저 ID들
//        List<Long> recommendUserIds = Arrays.asList(2L, 3L, 4L, 5L);
//
//        // Mock the CompletableFuture for matched users
//        CompletableFuture<List<Long>> matchedUserFuture = CompletableFuture.completedFuture(matchedUserIds);
//
//        // Mock the dependencies
//        when(getUserLikeQuery.findAllMatchedUserIdByUserId(testUserId))
//                .thenReturn(matchedUserIds);
//        when(getRecommendUserIdsQuery.getRecommendUserIds(testUserId, pageable))
//                .thenReturn(recommendUserIds);
//
//        // When
//        List<Long> result = portfolioService.getDistinctUserIds(testUserId, pageable);
//
//        // Then
//        assertThat(result)
//                .isNotNull()
//                .containsExactly(4L, 5L)
//                .doesNotContainAnyElementsOf(matchedUserIds);
//    }
//
//    @Test
//    @DisplayName("getDistinctUserIds - 모든 추천 유저가 이미 매칭된 경우")
//    void getDistinctUserIds_AllRecommendedUsersMatched() {
//        // Given
//        Long testUserId = 1L;
//        Pageable pageable = PageRequest.of(0, 10);
//
//        // 이미 매칭된 유저 ID들
//        List<Long> matchedUserIds = Arrays.asList(2L, 3L, 4L, 5L);
//
//        // 추천 유저 ID들
//        List<Long> recommendUserIds = Arrays.asList(2L, 3L, 4L, 5L);
//
//        // Mock the dependencies
//        when(getUserLikeQuery.findAllMatchedUserIdByUserId(testUserId))
//                .thenReturn(matchedUserIds);
//        when(getRecommendUserIdsQuery.getRecommendUserIds(testUserId, pageable))
//                .thenReturn(recommendUserIds);
//
//        // When
//        List<Long> result = portfolioService.getDistinctUserIds(testUserId, pageable);
//
//        // Then
//        assertThat(result).isEmpty();
//    }
//
//    @Test
//    @DisplayName("getDistinctUserIds - 추천 유저가 없는 경우")
//    void getDistinctUserIds_NoRecommendedUsers() {
//        // Given
//        Long testUserId = 1L;
//        Pageable pageable = PageRequest.of(0, 10);
//
//        // 이미 매칭된 유저 ID들
//        List<Long> matchedUserIds = Arrays.asList(2L, 3L);
//
//        // 추천 유저 ID들 (빈 리스트)
//        List<Long> recommendUserIds = Collections.emptyList();
//
//        // Mock the dependencies
//        when(getUserLikeQuery.findAllMatchedUserIdByUserId(testUserId))
//                .thenReturn(matchedUserIds);
//        when(getRecommendUserIdsQuery.getRecommendUserIds(testUserId, pageable))
//                .thenReturn(recommendUserIds);
//
//        // When
//        List<Long> result = portfolioService.getDistinctUserIds(testUserId, pageable);
//
//        // Then
//        assertThat(result).isEmpty();
//    }
//
//    @Test
//    @DisplayName("getDistinctUserIds - 비동기 작업 테스트")
//    void getDistinctUserIds_AsyncOperation() {
//        // Given
//        Long testUserId = 1L;
//        Pageable pageable = PageRequest.of(0, 10);
//
//        // 이미 매칭된 유저 ID들
//        List<Long> matchedUserIds = Arrays.asList(2L, 3L);
//
//        // 추천 유저 ID들
//        List<Long> recommendUserIds = Arrays.asList(4L, 5L, 6L);
//
//        // Mock the dependencies
//        when(getUserLikeQuery.findAllMatchedUserIdByUserId(testUserId))
//                .thenReturn(matchedUserIds);
//        when(getRecommendUserIdsQuery.getRecommendUserIds(testUserId, pageable))
//                .thenReturn(recommendUserIds);
//
//        // When
//        long startTime = System.currentTimeMillis();
//        List<Long> result = portfolioService.getDistinctUserIds(testUserId, pageable);
//        long endTime = System.currentTimeMillis();
//
//        // Then
//        assertThat(result)
//                .isNotNull()
//                .containsExactly(4L, 5L, 6L)
//                .doesNotContainAnyElementsOf(matchedUserIds);
//
//        // Verify that the operation was performed asynchronously
//        assertThat(endTime - startTime).isLessThan(1000);
//    }
//}
