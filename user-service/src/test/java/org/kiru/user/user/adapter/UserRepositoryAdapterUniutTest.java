package org.kiru.user.user.adapter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kiru.core.exception.EntityNotFoundException;
import org.kiru.core.user.talent.domain.Talent.TalentType;
import org.kiru.core.user.user.domain.User;
import org.kiru.core.user.user.entity.UserJpaEntity;
import org.kiru.core.user.userPortfolioItem.domain.UserPortfolio;
import org.kiru.core.user.userPortfolioItem.domain.UserPortfolioItem;
import org.kiru.core.user.userPortfolioItem.entity.UserPortfolioImg;
import org.kiru.core.user.userPurpose.domain.PurposeType;
import org.kiru.core.user.userPurpose.entity.UserPurpose;
import org.kiru.user.external.s3.ImageService;
import org.kiru.user.external.s3.S3Service;
import org.kiru.user.portfolio.repository.UserPortfolioRepository;
import org.kiru.user.user.dto.request.UserUpdateDto;
import org.kiru.user.user.repository.UserPurposeRepository;
import org.kiru.user.user.repository.UserRepository;
import org.kiru.user.user.repository.UserTalentRepository;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserRepositoryAdapterUniutTest {

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
  private S3Service s3Service;

  @InjectMocks
  private UserRepositoryAdapter adapter;

  private Long userId;
  private UserJpaEntity defaultEntity;

  @BeforeEach
  public void setUp() {
    userId = 1L;
    defaultEntity = UserJpaEntity.builder()
        .id(userId)
        .username("testUser")
        .email("test@example.com")
        .description("Test description")
        .instagramId("testInsta")
        .webUrl("http://example.com")
        .password("secret")
        .build();
  }

  @Test
  public void testGetUser_Success() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(defaultEntity));

    User user = adapter.getUser(userId);

    assertNotNull(user);
    assertEquals("testUser", user.getUsername());
    assertEquals("secret", user.getPassword());
  }

  @Test
  public void testGetUser_NotFound() {
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> adapter.getUser(userId));
  }

  @Test
  public void testSaveExistUser_PasswordNotCleared() {
    String originalPassword = "originalPassword";
    User user = User.builder()
        .id(userId)
        .username("testUser")
        .email("test@example.com")
        .description("Test description")
        .instagramId("testInsta")
        .webUrl("http://example.com")
        .password(originalPassword)
        .build();
    when(userRepository.existsById(userId)).thenReturn(true);

    UserJpaEntity savedEntity = UserJpaEntity.of(user);
    when(userRepository.save(any(UserJpaEntity.class))).thenReturn(savedEntity);

    User savedUser = adapter.saveExistUser(user);

    assertNotNull(savedUser);
    assertEquals(originalPassword, savedUser.getPassword(),
        "DB에 저장된 User의 password 필드가 null이어서는 안됩니다.");
  }

  @Test
  public void testSaveExistUser_UserNotFound() {
    Long nonExistingId = 2L;
    User user = User.builder().id(nonExistingId).build();
    when(userRepository.existsById(nonExistingId)).thenReturn(false);

    assertThrows(EntityNotFoundException.class, () -> adapter.saveExistUser(user));
  }

  @Test
  public void testUpdateUserPurposes() {
    UserUpdateDto dto = UserUpdateDto.builder()
        .userPurposes(List.of(1, 2, 3))
        .build();
    UserPurpose up1 = UserPurpose.builder().userId(userId).purposeType(PurposeType.fromIndex(1))
        .build();
    UserPurpose up2 = UserPurpose.builder().userId(userId).purposeType(PurposeType.fromIndex(2))
        .build();
    UserPurpose up3 = UserPurpose.builder().userId(userId).purposeType(PurposeType.fromIndex(3))
        .build();
    List<UserPurpose> savedPurposes = List.of(up1, up2, up3);
    when(userPurposeRepository.saveAll(any())).thenReturn(savedPurposes);

    List<PurposeType> result = adapter.updateUserPurposes(userId, dto);

    assertNotNull(result);
    assertEquals(3, result.size());
    assertTrue(result.contains(PurposeType.fromIndex(1)));
    assertTrue(result.contains(PurposeType.fromIndex(2)));
    assertTrue(result.contains(PurposeType.fromIndex(3)));
  }

  @Test
  public void testUpdateUserTalents() {
    List<TalentType> talents = List.of(TalentType.ARCHITECTURE, TalentType.COMPOSE, TalentType.DANCE);
    UserUpdateDto dto = UserUpdateDto.builder()
        .userTalents(talents)
        .build();
    var ut1 = org.kiru.core.user.talent.entity.UserTalent.builder().userId(userId)
        .talentType(TalentType.ARCHITECTURE).build();
    var ut2 = org.kiru.core.user.talent.entity.UserTalent.builder().userId(userId)
        .talentType(TalentType.COMPOSE).build();
    var ut3 = org.kiru.core.user.talent.entity.UserTalent.builder().userId(userId)
        .talentType(TalentType.DANCE).build();
    List<org.kiru.core.user.talent.entity.UserTalent> savedTalents = List.of(ut1, ut2, ut3);
    when(userTalentRepository.saveAll(any())).thenReturn(savedTalents);

    List<TalentType> result = adapter.updateUserTalents(userId, dto);

    assertNotNull(result);
    assertEquals(3, result.size());
    assertTrue(result.contains(TalentType.ARCHITECTURE));
    assertTrue(result.contains(TalentType.COMPOSE));
    assertTrue(result.contains(TalentType.DANCE));
  }

  @Test
  public void testUpdateUserPortfolioImages() {
    Map<Integer, Object> portfolioMap = Map.of(1, "dummyString");
    UserUpdateDto dto = UserUpdateDto.builder()
        .portfolio(portfolioMap)
        .username("testUser")
        .build();

    // 기존 포트폴리오 이미지 설정
    List<UserPortfolioImg> existingImages = List.of(
        UserPortfolioImg.builder()
            .userId(userId)
            .portfolioId(1L)
            .portfolioImageUrl("old-image1.jpg")
            .sequence(1)
            .build(),
        UserPortfolioImg.builder()
            .userId(userId)
            .portfolioId(1L)
            .portfolioImageUrl("old-image2.jpg")
            .sequence(2)
            .build()
    );
    when(userPortfolioRepository.findAllByUserId(userId)).thenReturn(existingImages);

    // 새로운 포트폴리오 이미지 설정
    UserPortfolioItem newItem = UserPortfolioImg.builder()
        .userId(userId)
        .portfolioId(1L)
        .portfolioImageUrl("new-image.jpg")
        .sequence(1)
        .build();
    List<UserPortfolioItem> newItems = List.of(newItem);
    when(imageService.saveImagesS3WithSequence(eq(portfolioMap), any(UserPortfolio.class), eq("testUser")))
        .thenReturn(newItems);

    // 유지되는 이미지 설정
    List<UserPortfolioItem> existingItems = List.of(
        UserPortfolioImg.builder()
            .userId(userId)
            .portfolioId(1L)
            .portfolioImageUrl("old-image2.jpg")
            .sequence(2)
            .build()
    );
    when(imageService.verifyExistingImages(eq(portfolioMap), any(UserPortfolio.class), eq("testUser")))
        .thenReturn(existingItems);

    when(userPortfolioRepository.saveAll(any())).thenReturn(Collections.emptyList());

    UserPortfolio portfolio = adapter.updateUserPortfolioImages(userId, dto);

    assertNotNull(portfolio);

    // S3 이미지 삭제 검증
    ArgumentCaptor<Set<String>> deletedImageUrlsCaptor = ArgumentCaptor.forClass(Set.class);
    verify(s3Service).deleteImages(deletedImageUrlsCaptor.capture());
    Set<String> deletedImageUrls = deletedImageUrlsCaptor.getValue();
    assertEquals(1, deletedImageUrls.size());
    assertTrue(deletedImageUrls.contains("old-image1.jpg"));
    assertFalse(deletedImageUrls.contains("old-image2.jpg"));
    assertFalse(deletedImageUrls.contains("new-image.jpg"));
  }

  @Test
  public void testGetUserPurposes() {
    UserPurpose up1 = UserPurpose.builder().userId(userId).purposeType(PurposeType.fromIndex(1))
        .build();
    UserPurpose up2 = UserPurpose.builder().userId(userId).purposeType(PurposeType.fromIndex(2))
        .build();
    List<UserPurpose> purposes = List.of(up1, up2);
    when(userPurposeRepository.findAllByUserId(userId)).thenReturn(purposes);

    List<PurposeType> result = adapter.getUserPurposes(userId);

    assertNotNull(result);
    assertEquals(2, result.size());
    assertTrue(result.contains(PurposeType.fromIndex(1)));
    assertTrue(result.contains(PurposeType.fromIndex(2)));
  }

  @Test
  public void testGetUserTalents() {
    var ut1 = org.kiru.core.user.talent.entity.UserTalent.builder().userId(userId)
        .talentType(TalentType.ARCHITECTURE).build();
    var ut2 = org.kiru.core.user.talent.entity.UserTalent.builder().userId(userId)
        .talentType(TalentType.DANCE).build();
    List<org.kiru.core.user.talent.entity.UserTalent> talents = List.of(ut1, ut2);
    when(userTalentRepository.findAllByUserId(userId)).thenReturn(talents);

    List<TalentType> result = adapter.getUserTalents(userId);

    assertNotNull(result);
    assertEquals(2, result.size());
    assertTrue(result.contains(TalentType.ARCHITECTURE));
    assertTrue(result.contains(TalentType.DANCE));
  }

  @Test
  public void testGetUserPortfolioByUserId() {
    UserPortfolioImg img1 = UserPortfolioImg.builder().build();
    UserPortfolioImg img2 = UserPortfolioImg.builder().build();
    List<UserPortfolioImg> imgs = List.of(img1, img2);
    when(userPortfolioRepository.findAllByUserId(userId)).thenReturn(imgs);

    List<UserPortfolioItem> result = adapter.getUserPortfolioByUserId(userId);

    assertNotNull(result);
    assertEquals(2, result.size());
  }
}
