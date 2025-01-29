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
import org.kiru.core.user.userPortfolioItem.domain.UserPortfolioItem;
import org.kiru.core.user.userPortfolioItem.entity.UserPortfolioImg;
import org.kiru.user.portfolio.adapter.UserPortfolioJpaAdapter;
import org.kiru.user.portfolio.adapter.dto.UserPortfolioProjection;
import org.kiru.user.portfolio.dto.res.UserPortfolioResDto;
import org.kiru.user.portfolio.repository.UserPortfolioRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetUserPortfoliosQueryTest {

    @Mock
    private UserPortfolioRepository userPortfolioRepository;

    @InjectMocks
    private UserPortfolioJpaAdapter getUserPortfoliosQuery;

    private List<UserPortfolioImg> testPortfolios;
    List<UserPortfolioProjection> singleUserPortfolio;

    @BeforeEach
    void setUp() {
        testPortfolios = Arrays.asList(
                UserPortfolioImg.builder()
                        .userId(1L)
                        .userName("user1")
                        .portfolioId(1L)
                        .portfolioImageUrl("image1.jpg")
                        .sequence(1)
                        .build(),
                UserPortfolioImg.builder()
                        .userId(2L)
                        .userName("user2")
                        .portfolioId(2L)
                        .portfolioImageUrl("image3.jpg")
                        .sequence(1)
                        .build()
        );

        singleUserPortfolio = Arrays.asList(
                new UserPortfolioProjection() {
                    @Override
                    public Long getPortfolioId() {
                        return 1L;
                    }

                    @Override
                    public Long getUserId() {
                        return 1L;
                    }

                    @Override
                    public String getUsername() {
                        return "user1";
                    }

                    @Override
                    public String getPortfolioImageUrl() {
                        return "image1.jpg,image2.jpg";
                    }
                },
                new UserPortfolioProjection() {
                    @Override
                    public Long getPortfolioId() {
                        return 2L;
                    }

                    @Override
                    public Long getUserId() {
                        return 2L;
                    }

                    @Override
                    public String getUsername() {
                        return "user2";
                    }

                    @Override
                    public String getPortfolioImageUrl() {
                        return "image1.jpg,image2.jpg";
                    }
                }
        );
    }

    @Test
    @DisplayName("사용자 포트폴리오 조회 - 성공")
    void findAllPortfoliosByUserIds_Success() {
        // Given
        List<Long> userIds = Arrays.asList(1L, 2L);
        when(userPortfolioRepository.findAllPortfoliosByUserIds(userIds)).thenReturn(singleUserPortfolio);

        // When
        List<UserPortfolioResDto> result = getUserPortfoliosQuery.findAllPortfoliosByUserIds(userIds);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUserId()).isEqualTo(1L);
        assertThat(result.get(0).getPortfolioImageUrl()).hasSize(2);
        assertThat(result.get(1).getUserId()).isEqualTo(2L);
        assertThat(result.get(1).getPortfolioImageUrl()).hasSize(2);
    }

    @Test
    @DisplayName("사용자 포트폴리오 조회 - 빈 결과")
    void findAllPortfoliosByUserIds_EmptyResult() {
        // Given
        List<Long> userIds = Arrays.asList(999L);
        when(userPortfolioRepository.findAllPortfoliosByUserIds(userIds)).thenReturn(Collections.emptyList());
        // When
        List<UserPortfolioResDto> result = getUserPortfoliosQuery.findAllPortfoliosByUserIds(userIds);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("사용자 포트폴리오url이 가장 작은 itemUrl인지 확인")
    void getUserPortfoliosWithThumbnail_EmptyResult() {
        // Given
        List<Long> userIds = Arrays.asList(1L,2L);
        when(userPortfolioRepository.findAllByUserIdInWithItemUrlMinSequence(userIds)).thenReturn(testPortfolios);
        // When
        List<UserPortfolioItem> result = getUserPortfoliosQuery.getUserPortfoliosWithMinSequence(userIds);
        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getSequence()).isEqualTo(1);
        assertThat(result.get(1).getSequence()).isEqualTo(1);
    }
}