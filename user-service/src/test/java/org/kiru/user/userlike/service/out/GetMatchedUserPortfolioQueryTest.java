package org.kiru.user.userlike.service.out;

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
class GetMatchedUserPortfolioQueryTest {

    @Mock
    private GetMatchedUserPortfolioQuery getMatchedUserPortfolioQuery;

    private List<UserPortfolioResDto> testMatchedPortfolios;

    @BeforeEach
    void setUp() {
        testMatchedPortfolios = Arrays.asList(
            UserPortfolioResDto.builder()
                .userId(1L)
                .username("matchedUser1")
                .portfolioId(1L)
                .portfolioImages(Arrays.asList("match1.jpg", "match2.jpg"))
                .build(),
            UserPortfolioResDto.builder()
                .userId(2L)
                .username("matchedUser2")
                .portfolioId(2L)
                .portfolioImages(Arrays.asList("match3.jpg", "match4.jpg"))
                .build()
        );
    }

    @Test
    @DisplayName("매칭된 사용자 포트폴리오 조회 - 성공")
    void findByUserIds_Success() {
        // Given
        List<Long> matchedUserIds = Arrays.asList(1L, 2L);
        when(getMatchedUserPortfolioQuery.findByUserIds(matchedUserIds)).thenReturn(testMatchedPortfolios);

        // When
        List<UserPortfolioResDto> result = getMatchedUserPortfolioQuery.findByUserIds(matchedUserIds);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUserId()).isEqualTo(1L);
        assertThat(result.get(0).getPortfolioImages()).hasSize(2);
        assertThat(result.get(1).getUserId()).isEqualTo(2L);
        assertThat(result.get(1).getPortfolioImages()).hasSize(2);
    }

    @Test
    @DisplayName("매칭된 사용자 포트폴리오 조회 - 빈 결과")
    void findByUserIds_EmptyResult() {
        // Given
        List<Long> unmatchedUserIds = Arrays.asList(999L);
        when(getMatchedUserPortfolioQuery.findByUserIds(unmatchedUserIds)).thenReturn(Collections.emptyList());

        // When
        List<UserPortfolioResDto> result = getMatchedUserPortfolioQuery.findByUserIds(unmatchedUserIds);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("매칭된 사용자 포트폴리오 조회 - 단일 사용자")
    void findByUserIds_SingleUser() {
        // Given
        List<Long> singleUserId = Arrays.asList(1L);
        List<UserPortfolioResDto> singleUserPortfolio = Arrays.asList(testMatchedPortfolios.get(0));
        when(getMatchedUserPortfolioQuery.findByUserIds(singleUserId)).thenReturn(singleUserPortfolio);

        // When
        List<UserPortfolioResDto> result = getMatchedUserPortfolioQuery.findByUserIds(singleUserId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(1L);
        assertThat(result.get(0).getUsername()).isEqualTo("matchedUser1");
        assertThat(result.get(0).getPortfolioImages()).hasSize(2);
    }
}
