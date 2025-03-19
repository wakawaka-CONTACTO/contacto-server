package org.kiru.user.user.service.out;


import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
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
import org.kiru.core.user.userPortfolioItem.domain.UserPortfolio;
import org.kiru.core.user.userPortfolioItem.domain.UserPortfolioItem;
import org.kiru.core.user.userPortfolioItem.entity.UserPortfolioImg;
import org.kiru.core.user.userPurpose.domain.PurposeType;
import org.kiru.core.user.userPurpose.entity.UserPurpose;
import org.kiru.user.external.s3.ImageService;
import org.kiru.user.portfolio.repository.UserPortfolioRepository;
import org.kiru.user.user.adapter.UserRepositoryAdapter;
import org.kiru.user.user.dto.request.UserUpdateDto;
import org.kiru.user.user.dto.request.UserUpdatePwdDto;
import org.kiru.user.user.repository.UserPurposeRepository;
import org.kiru.user.user.repository.UserTalentRepository;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class UserUpdatePortTest {

    @InjectMocks
    private UserRepositoryAdapter userUpdatePort;

    @Mock
    private ImageService imageService;

    @Mock
    private UserPortfolioRepository userPortfolioRepository;

    @Mock
    private UserPurposeRepository userPurposeRepository;
    @Mock
    private UserTalentRepository userTalentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserUpdateDto userUpdateDto;

    @BeforeEach
    void setUp() {
        Map<Integer, Object> items = new HashMap<>();
        MultipartFile mockFile = new MockMultipartFile("file", "filename.jpg", "image/jpeg", new byte[0]);
        MultipartFile mockFile2 = new MockMultipartFile("file", "filename.jpg", "image/jpeg", new byte[0]);
        MultipartFile mockFile3 = new MockMultipartFile("file", "filename.jpg", "image/jpeg", new byte[0]);
        String mockString = "string";
        String mockString2 = "string2";
        items.put(1, mockFile);
        items.put(2, mockString);
        items.put(3, mockFile2);
        items.put(4, mockString2);
        items.put(5, mockFile3);
        userUpdateDto = UserUpdateDto.builder()
                .email("test@example.com")
                .username("user1")
                .userPurposes(List.of(1, 2, 3))
                .userTalents(List.of(TalentType.ARCHITECTURE, TalentType.COMPOSE, TalentType.DANCE))
                .portfolio(
                        items
                )
                .build();
    }

    @Test
    @DisplayName("사용자 비밀번호 업데이트 테스트")
    void updateUserPwdTest() {
        UserJpaEntity existingUser = UserJpaEntity.of(
                User.builder()
                        .id(1L)
                        .username("testUser")
                        .loginType(LoginType.KAKAO)
                        .email("test@example.com")
                        .description("Test description")
                        .password("oldPassword")
                        .build()
        );
        UserUpdatePwdDto userUpdatePwdDto =  new UserUpdatePwdDto("newEmail@example.com", "newPassword");

        when(passwordEncoder.encode(anyString())).thenReturn("encodedNewPassword");
        // When
        Boolean result = userUpdatePort.updateUserPwd(existingUser, userUpdatePwdDto);

        // Then
        assertThat(result).isTrue();
        assertThat(existingUser.getPassword()).isEqualTo("encodedNewPassword");
    }

    @Test
    @DisplayName("포트폴리오 이미지 업데이트 - MultipartFile과 String URL 혼합")
    void updateUserPortfolioImages_MixedTypes() {
        // Given
        List<UserPortfolioItem> savedImages = Arrays.asList(
                UserPortfolioImg.builder()
                        .userId(1L)
                        .portfolioId(1L)
                        .portfolioImageUrl("new-image.jpg")
                        .sequence(1)
                        .build()
        );

        List<UserPortfolioImg> existImagesEntity = Arrays.asList(
                UserPortfolioImg.builder()
                        .id(1L)
                        .userId(1L)
                        .portfolioId(1L)
                        .portfolioImageUrl("exist-image.jpg")
                        .sequence(1)
                        .build(),
                UserPortfolioImg.builder()
                        .id(2L)
                        .userId(1L)
                        .portfolioId(1L)
                        .portfolioImageUrl("exist-image2.jpg")
                        .sequence(2)
                        .build(),
                UserPortfolioImg.builder()
                        .id(3L)
                        .userId(1L)
                        .portfolioId(1L)
                        .portfolioImageUrl("exist-image3.jpg")
                        .sequence(3)
                        .build()
        );

        List<UserPortfolioImg> savedImagesEntity = Arrays.asList(
            UserPortfolioImg.builder()
                .id(1L)
                .userId(1L)
                .portfolioId(1L)
                .portfolioImageUrl("new-image.jpg")
                .sequence(1)
                .build(),
            UserPortfolioImg.builder()
                .id(2L)
                .userId(1L)
                .portfolioId(1L)
                .portfolioImageUrl("exist-image2.jpg")
                .sequence(2)
                .build(),
            UserPortfolioImg.builder()
                .id(3L)
                .userId(1L)
                .portfolioId(1L)
                .portfolioImageUrl("exist-image3.jpg")
                .sequence(3)
                .build()
        );
        when(imageService.saveImagesS3WithSequence(anyMap(), any(),any()))
                .thenReturn(savedImages);
        when(userPortfolioRepository.saveAll(any()))
                .thenReturn(savedImagesEntity);

        // When
        UserPortfolio result = userUpdatePort.updateUserPortfolioImages(1L, userUpdateDto);

        // Then
        assertThat(result.getPortfolioItems()).hasSize(3);
        assertThat(result.getPortfolioItems()).isSortedAccordingTo(
                java.util.Comparator.comparing(UserPortfolioItem::getSequence));
        assertThat(result.getPortfolioItems().getFirst().getItemUrl()).isEqualTo("new-image.jpg");
        verify(imageService).saveImagesS3WithSequence(anyMap(), any(UserPortfolio.class), anyString());
    }

    @Test
    void updateUserPurposes() {
        when(userPurposeRepository.saveAll(any()))
                .thenReturn(userUpdateDto.getUserPurposes().stream()
                        .map(purposeType -> UserPurpose.builder().userId(1L)
                                .purposeType(PurposeType.fromIndex(purposeType))
                                .build()).toList());
        //when
        List<PurposeType> result = userUpdatePort.updateUserPurposes(1L, userUpdateDto);

        //then
        assertThat(result).hasSize(3);
        assertThat(result).containsExactlyElementsOf(userUpdateDto.getUserPurposes().stream().map(PurposeType::fromIndex).toList());
        InOrder inOrder = inOrder(userPurposeRepository);
        inOrder.verify(userPurposeRepository).deleteAllByUserId(any()); // 먼저 delete 호출
        inOrder.verify(userPurposeRepository).saveAll(any());
    }

    @Test
    @DisplayName("유저 재능목록 업데이트")
    void updateUserTalents() {
        //given
        when(userTalentRepository.saveAll(any()))
                .thenReturn(userUpdateDto.getUserTalents().stream()
                        .map(talent -> UserTalent.builder().userId(1L).talentType(talent).build())
                        .toList());
        //when
        List<TalentType> result = userUpdatePort.updateUserTalents(1L, userUpdateDto);

        //then
        assertThat(result).hasSize(3);
        assertThat(result).isEqualTo(userUpdateDto.getUserTalents());
        assertThat(result).containsExactlyElementsOf(userUpdateDto.getUserTalents());
        InOrder inOrder = inOrder(userTalentRepository);
        inOrder.verify(userTalentRepository).deleteAllByUserId(any()); // 먼저 delete 호출
        inOrder.verify(userTalentRepository).saveAll(any());
    }
}