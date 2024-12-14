package org.kiru.user.user.service.out;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kiru.core.user.talent.domain.Talent.TalentType;
import org.kiru.core.user.talent.entity.UserTalent;
import org.kiru.core.user.user.domain.LoginType;
import org.kiru.core.user.user.domain.User;
import org.kiru.core.user.user.entity.UserJpaEntity;
import org.kiru.core.user.userPortfolioImg.entity.UserPortfolioImg;
import org.kiru.core.user.userPurpose.entity.UserPurpose;
import org.kiru.user.external.s3.ImageService;
import org.kiru.user.portfolio.repository.UserPortfolioRepository;
import org.kiru.user.user.adapter.UserRepositoryAdapter;
import org.kiru.user.user.dto.request.UserUpdateDto;
import org.kiru.user.user.repository.UserPurposeRepository;
import org.kiru.user.user.repository.UserTalentRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class UserUpdateUseCaseTest {

    @InjectMocks
    private UserRepositoryAdapter userUpdateUseCase;

    @Mock
    private MultipartFile multipartFile;

    @Mock
    private UserPurposeRepository userPurposeRepository;

    @Mock
    private UserTalentRepository userTalentRepository;
    @Mock
    private UserPortfolioRepository userPortfolioRepository;
    @Mock
    private ImageService imageService;

    private UserJpaEntity testUser;
    private UserUpdateDto testUpdateDto;

    @BeforeEach
    void setUp() {
        testUser = UserJpaEntity.of(
                User.builder()
                        .id(1L)
                        .username("testUser")
                        .loginType(LoginType.KAKAO)
                        .email("userEmail@email")
                        .description("userDescription")
                        .instagramId("asdfasd")
                        .webUrl("http://weburl.com")
                        .build()
        );

        testUpdateDto = new UserUpdateDto();
        testUpdateDto.setUserPurposes(Arrays.asList(1, 2));
        testUpdateDto.setUserTalents(Arrays.asList(TalentType.ACT, TalentType.ADVERTISING));

        Map<Integer, Object> portfolioImages = new HashMap<>();
        portfolioImages.put(1, multipartFile);
        testUpdateDto.setPortfolioImages(portfolioImages);
    }

    @Test
    @DisplayName("사용자 목적 업데이트 - 성공")
    void updateUserPurposes_Success() {
        // Given
        doNothing().when(userPurposeRepository).deleteAllByUserId(1L);
        // When
        List<UserPurpose> result = userUpdateUseCase.updateUserPurposes(1L, testUpdateDto);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.stream().map(i->i.getPurposeType().getIndex())).isEqualTo(testUpdateDto.getUserPurposes());
    }
    @Test
    @DisplayName("사용자 재능 업데이트 - 성공")
    void updateUserTalents_Success() {
        // Given
        doNothing().when(userTalentRepository).deleteAllByUserId(1L);
        // When
        List<UserTalent> result = userUpdateUseCase.updateUserTalents(1L, testUpdateDto);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTalentType()).isEqualTo(TalentType.ACT);
        assertThat(result.get(1).getTalentType()).isEqualTo(TalentType.ADVERTISING);
    }
}