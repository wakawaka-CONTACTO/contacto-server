package org.kiru.user.user.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kiru.core.exception.ContactoException;
import org.kiru.core.exception.EntityNotFoundException;
import org.kiru.core.user.talent.entity.UserTalent;
import org.kiru.core.user.user.domain.LoginType;
import org.kiru.core.user.user.domain.User;
import org.kiru.core.user.user.entity.UserJpaEntity;
import org.kiru.core.user.userPortfolioImg.entity.UserPortfolioImg;
import org.kiru.core.user.userPurpose.domain.PurposeType;
import org.kiru.core.user.userPurpose.entity.UserPurpose;
import org.kiru.user.external.s3.ImageService;
import org.kiru.user.portfolio.repository.UserPortfolioRepository;
import org.kiru.user.user.dto.request.UserUpdateDto;
import org.kiru.user.user.repository.UserPurposeRepository;
import org.kiru.user.user.repository.UserRepository;
import org.kiru.user.user.repository.UserTalentRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class UserRepositoryAdapterTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserPurposeRepository userPurposeRepository;
    @Mock
    private UserTalentRepository userTalentRepository;
    @Mock
    private UserPortfolioRepository userPortfolioRepository;
    @Mock
    private ImageService imageService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private UserRepositoryAdapter userRepositoryAdapter;

    private UserJpaEntity testUser;
    private UserUpdateDto testUpdateDto;
    private Long userId = 1L;
    private Long portfolioId = 1L;

    @BeforeEach
    void setUp() {
        testUser = UserJpaEntity.of(
            User.builder()
                .id(userId)
                .username("testUser")
                .loginType(LoginType.KAKAO)
                .email("test@example.com")
                .description("Test description")
                .build()
        );

        testUpdateDto = new UserUpdateDto();
        Map<Integer, Object> portfolioImages = new HashMap<>();
        portfolioImages.put(1, multipartFile);
        testUpdateDto.setPortfolioImages(portfolioImages);

        when(userPortfolioRepository.findAllByUserId(userId))
            .thenReturn(List.of(UserPortfolioImg.builder()
                .portfolioId(portfolioId)
                .build()));
    }

    @Test
    @DisplayName("포트폴리오 이미지 업데이트 - MultipartFile만 있는 경우")
    void updateUserPortfolioImages_OnlyMultipartFiles() {
        // Given
        Map<Integer, Object> portfolioImages = new HashMap<>();
        portfolioImages.put(1, multipartFile);
        portfolioImages.put(2, multipartFile);
        testUpdateDto.setPortfolioImages(portfolioImages);

        List<UserPortfolioImg> savedImages = Arrays.asList(
            UserPortfolioImg.builder()
                .userId(userId)
                .portfolioId(portfolioId)
                .portfolioImageUrl("new-image-1.jpg")
                .sequence(1)
                .build(),
            UserPortfolioImg.builder()
                .userId(userId)
                .portfolioId(portfolioId)
                .portfolioImageUrl("new-image-2.jpg")
                .sequence(2)
                .build()
        );

        when(imageService.saveImagesWithSequence(anyMap(), anyLong(), anyLong()))
            .thenReturn(savedImages);

        // When
        List<UserPortfolioImg> result = userRepositoryAdapter.updateUserPortfolioImages(userId, testUpdateDto);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).isSortedAccordingTo(
            java.util.Comparator.comparing(UserPortfolioImg::getSequence));
        verify(userPortfolioRepository).deleteAllByUserId(userId);
        verify(imageService).saveImagesWithSequence(anyMap(), anyLong(), anyLong());
    }

    @Test
    @DisplayName("포트폴리오 이미지 업데이트 - String URL만 있는 경우")
    void updateUserPortfolioImages_OnlyStringUrls() {
        // Given
        Map<Integer, Object> portfolioImages = new HashMap<>();
        portfolioImages.put(2, "existing-image-2.jpg");
        portfolioImages.put(1, "existing-image-1.jpg");
        testUpdateDto.setPortfolioImages(portfolioImages);

        // When
        List<UserPortfolioImg> result = userRepositoryAdapter.updateUserPortfolioImages(userId, testUpdateDto);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).isSortedAccordingTo(
            java.util.Comparator.comparing(UserPortfolioImg::getSequence));
        assertThat(result.get(0).getSequence()).isEqualTo(1);
        assertThat(result.get(0).getPortfolioImageUrl()).isEqualTo("existing-image-1.jpg");
        assertThat(result.get(1).getSequence()).isEqualTo(2);
        assertThat(result.get(1).getPortfolioImageUrl()).isEqualTo("existing-image-2.jpg");
        verify(userPortfolioRepository).deleteAllByUserId(userId);
    }

    @Test
    @DisplayName("포트폴리오 이미지 업데이트 - MultipartFile과 String URL 혼합")
    void updateUserPortfolioImages_MixedTypes() {
        // Given
        Map<Integer, Object> portfolioImages = new HashMap<>();
        portfolioImages.put(1, multipartFile);
        portfolioImages.put(2, "existing-image.jpg");
        testUpdateDto.setPortfolioImages(portfolioImages);

        List<UserPortfolioImg> savedImages = Arrays.asList(
            UserPortfolioImg.builder()
                .userId(userId)
                .portfolioId(portfolioId)
                .portfolioImageUrl("new-image.jpg")
                .sequence(1)
                .build()
        );

        when(imageService.saveImagesWithSequence(anyMap(), anyLong(), anyLong()))
            .thenReturn(savedImages);

        // When
        List<UserPortfolioImg> result = userRepositoryAdapter.updateUserPortfolioImages(userId, testUpdateDto);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).isSortedAccordingTo(
            java.util.Comparator.comparing(UserPortfolioImg::getSequence));
        assertThat(result.get(0).getPortfolioImageUrl()).isEqualTo("new-image.jpg");
        assertThat(result.get(1).getPortfolioImageUrl()).isEqualTo("existing-image.jpg");
        verify(userPortfolioRepository).deleteAllByUserId(userId);
        verify(imageService).saveImagesWithSequence(anyMap(), anyLong(), anyLong());
    }

    @Test
    @DisplayName("포트폴리오 이미지 업데이트 - 잘못된 타입")
    void updateUserPortfolioImages_InvalidType() {
        // Given
        Map<Integer, Object> portfolioImages = new HashMap<>();
        portfolioImages.put(1, 123); // Invalid type
        testUpdateDto.setPortfolioImages(portfolioImages);

        // When & Then
        assertThatThrownBy(() -> userRepositoryAdapter.updateUserPortfolioImages(userId, testUpdateDto))
            .isInstanceOf(ContactoException.class);
    }

    @Test
    @DisplayName("포트폴리오 이미지 업데이트 - 시퀀스 순서 확인")
    void updateUserPortfolioImages_SequenceOrder() {
        // Given
        Map<Integer, Object> portfolioImages = new HashMap<>();
        portfolioImages.put(3, "image3.jpg");
        portfolioImages.put(1, "image1.jpg");
        portfolioImages.put(2, "image2.jpg");
        testUpdateDto.setPortfolioImages(portfolioImages);

        // When
        List<UserPortfolioImg> result = userRepositoryAdapter.updateUserPortfolioImages(userId, testUpdateDto);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).isSortedAccordingTo(
            java.util.Comparator.comparing(UserPortfolioImg::getSequence));
        assertThat(result.get(0).getSequence()).isEqualTo(1);
        assertThat(result.get(1).getSequence()).isEqualTo(2);
        assertThat(result.get(2).getSequence()).isEqualTo(3);
    }
}
