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
import org.kiru.user.portfolio.adapter.UserPortfolioJpaAdapter;
import org.kiru.user.portfolio.adapter.dto.UserPortfolioProjection;
import org.kiru.user.portfolio.dto.res.UserPortfolioResDto;
import org.kiru.user.portfolio.repository.UserPortfolioRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetMatchedUserPortfolioQueryTest {

    @InjectMocks
    private UserPortfolioJpaAdapter getMatchedUserPortfolioQuery;

    @Mock
    private UserPortfolioRepository userPortfolioRepository;

    private  List<UserPortfolioProjection> testMatchedPortfoliosProjection;

    @BeforeEach
    void setUp() {
        testMatchedPortfoliosProjection = Arrays.asList(
                new UserIdAndName(1L, 1L, "matchedUser1", "match1.jpg,match2.jpg"),
                new UserIdAndName(2L, 2L, "matchedUser2", "match3.jpg,match4.jpg")
        );
    }

    @Test
    @DisplayName("매칭된 사용자 포트폴리오 조회 - 성공")
    void findByUserIds_Success() {
        // Given
        List<Long> matchedUserIds = Arrays.asList(1L, 2L);
        when(userPortfolioRepository.findAllPortfoliosByUserIds(matchedUserIds)).thenReturn(testMatchedPortfoliosProjection);

        // When
        List<UserPortfolioResDto> result = getMatchedUserPortfolioQuery.findByUserIds(matchedUserIds);
        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUserId()).isEqualTo(1L);
        assertThat(result.get(0).getUsername()).isEqualTo("matchedUser1");
        assertThat(result.get(0).getPortfolioImageUrl()).hasSize(2);
        assertThat(result.get(0).getPortfolioImageUrl().contains("match1.jpg")).isTrue();
        assertThat(result.get(0).getPortfolioImageUrl().contains("match2.jpg")).isTrue();

        assertThat(result.get(1).getUserId()).isEqualTo(2L);
        assertThat(result.get(1).getPortfolioImageUrl()).hasSize(2);
        assertThat(result.get(1).getUsername()).isEqualTo("matchedUser2");
        assertThat(result.get(1).getPortfolioImageUrl().contains("match3.jpg")).isTrue();
        assertThat(result.get(1).getPortfolioImageUrl().contains("match4.jpg")).isTrue();
    }

    @Test
    @DisplayName("매칭된 사용자 포트폴리오 조회 - 빈 결과")
    void findByUserIds_EmptyResult() {
        // Given
        List<Long> unmatchedUserIds = List.of(1L,2L);
        when(userPortfolioRepository.findAllPortfoliosByUserIds(unmatchedUserIds)).thenReturn(Collections.emptyList());
        // When
        List<UserPortfolioResDto> result = getMatchedUserPortfolioQuery.findByUserIds(unmatchedUserIds);
        // Then
        assertThat(result).isEmpty();
    }

    public class UserIdAndName implements UserPortfolioProjection {
        private final Long portfolioId;
        private final Long userId;
        private final String username;
        private final String portfolioImageUrl;

        public UserIdAndName(Long portfolioId, Long userId, String username, String portfolioImageUrl) {
            this.portfolioId = portfolioId;
            this.userId = userId;
            this.username = username;
            this.portfolioImageUrl = portfolioImageUrl;
        }

        @Override
        public Long getPortfolioId() {
            return this.portfolioId;
        }

        @Override
        public Long getUserId() {
            return this.userId;
        }

        @Override
        public String getUsername() {
            return this.username;
        }

        @Override
        public String getPortfolioImageUrl() {
            return this.portfolioImageUrl;
        }

    }
}
