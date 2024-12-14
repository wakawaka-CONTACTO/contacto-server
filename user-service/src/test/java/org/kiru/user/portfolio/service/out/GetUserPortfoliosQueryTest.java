package org.kiru.user.portfolio.service.out;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kiru.user.portfolio.dto.res.UserPortfolioResDto;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetUserPortfoliosQueryTest {

    @Mock
    private GetUserPortfoliosQuery getUserPortfoliosQuery;

    private List<UserPortfolioResDto> testPortfolios;

    @BeforeEach
    void setUp() {
        testPortfolios = Arrays.asList(
            UserPortfolioResDto.builder()
                .userId(1L)
                .username("user1")
                .portfolioId(1L)
                .portfolioImages(Arrays.asList("image1.jpg", "image2.jpg"))
                .build(),
            UserPortfolioResDto.builder()
                .userId(2L)
                .username("user2")
                .portfolioId(2L)
                .portfolioImages(Arrays.asList("image3.jpg", "image4.jpg"))
                .build()
        );
    }

    @Test
    @DisplayName("사용자 포트폴리오 조회 - 성공")
    void findAllPortfoliosByUserIds_Success() {
        // Given
        List<Long> userIds = Arrays.asList(1L, 2L);
        when(getUserPortfoliosQuery.findAllPortfoliosByUserIds(userIds)).thenReturn(testPortfolios);

        // When
        List<UserPortfolioResDto> result = getUserPortfoliosQuery.findAllPortfoliosByUserIds(userIds);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUserId()).isEqualTo(1L);
        assertThat(result.get(0).getPortfolioImages()).hasSize(2);
        assertThat(result.get(1).getUserId()).isEqualTo(2L);
        assertThat(result.get(1).getPortfolioImages()).hasSize(2);
    }

    @Test
    @DisplayName("사용자 포트폴리오 조회 - 빈 결과")
    void findAllPortfoliosByUserIds_EmptyResult() {
        // Given
        List<Long> userIds = Arrays.asList(999L);
        when(getUserPortfoliosQuery.findAllPortfoliosByUserIds(userIds)).thenReturn(Collections.emptyList());
        // When
        List<UserPortfolioResDto> result = getUserPortfoliosQuery.findAllPortfoliosByUserIds(userIds);
        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("사용자 포트폴리오 조회 - 단일 사용자")
    void findAllPortfoliosByUserIds_SingleUser() {
        // Given
        List<Long> userIds = Arrays.asList(1L);
        List<UserPortfolioResDto> singleUserPortfolio = Arrays.asList(testPortfolios.getFirst());
        when(getUserPortfoliosQuery.findAllPortfoliosByUserIds(userIds)).thenReturn(singleUserPortfolio);

        // When
        List<UserPortfolioResDto> result = getUserPortfoliosQuery.findAllPortfoliosByUserIds(userIds);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(1L);
        assertThat(result.get(0).getPortfolioImages()).hasSize(2);
    }
}
