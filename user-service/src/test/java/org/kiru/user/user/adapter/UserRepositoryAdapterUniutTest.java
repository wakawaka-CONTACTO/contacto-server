package org.kiru.user.user.adapter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import org.kiru.user.portfolio.repository.UserPortfolioRepository;
import org.kiru.user.user.dto.request.UserUpdateDto;

import org.kiru.user.user.repository.UserPurposeRepository;
import org.kiru.user.user.repository.UserRepository;
import org.kiru.user.user.repository.UserTalentRepository;
import org.kiru.user.user.service.out.UserQueryWithCache;
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

  // UserQueryWithCache는 캐시관련 인터페이스로, 여기서는 saveExistUser 호출 시 스텁 처리에 활용합니다.
  @Mock
  private UserQueryWithCache userQueryWithCache;

  @InjectMocks
  private UserRepositoryAdapter adapter;

  // --- getUser 테스트 ---
  @Test
  public void testGetUser_Success() {
    Long userId = 1L;
    UserJpaEntity entity = UserJpaEntity.builder()
        .id(userId)
        .username("testUser")
        .email("test@example.com")
        .description("Test description")
        .instagramId("testInsta")
        .webUrl("http://example.com")
        .password("secret")
        .build();
    when(userRepository.findById(userId)).thenReturn(Optional.of(entity));

    User user = adapter.getUser(userId);

    assertNotNull(user);
    assertEquals("testUser", user.getUsername());
    assertEquals("secret", user.getPassword());
  }

  @Test
  public void testGetUser_NotFound() {
    Long userId = 1L;
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> adapter.getUser(userId));
  }

  // --- saveExistUser 테스트 ---
  @Test
  public void testSaveExistUser_PasswordNotCleared() {
    Long userId = 1L;
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

    // 저장시 변환된 엔티티를 그대로 반환하도록 스텁 처리
    UserJpaEntity savedEntity = UserJpaEntity.of(user);
    when(userRepository.save(any(UserJpaEntity.class))).thenReturn(savedEntity);

    User savedUser = adapter.saveExistUser(user);

    assertNotNull(savedUser);
    assertEquals(originalPassword, savedUser.getPassword(),
        "DB에 저장된 User의 password 필드가 null이어서는 안됩니다.");
  }

  @Test
  public void testSaveExistUser_UserNotFound() {
    Long userId = 2L;
    User user = User.builder().id(userId).build();
    when(userRepository.existsById(userId)).thenReturn(false);

    assertThrows(EntityNotFoundException.class, () -> adapter.saveExistUser(user));
  }

  // --- updateUserPurposes 테스트 ---
  @Test
  public void testUpdateUserPurposes() {
    Long userId = 1L;
    // UserUpdateDto에 userPurposes로 [1, 2, 3] 전달
    UserUpdateDto dto = UserUpdateDto.builder()
        .userPurposes(List.of(1, 2, 3))
        .build();
    // dummy UserPurpose 엔티티 생성
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

  // --- updateUserTalents 테스트 ---
  @Test
  public void testUpdateUserTalents() {
    Long userId = 1L;
    List<TalentType> talents = List.of(TalentType.ARCHITECTURE, TalentType.COMPOSE,
        TalentType.DANCE);
    UserUpdateDto dto = UserUpdateDto.builder()
        .userTalents(talents)
        .build();
    // dummy UserTalent 엔티티 생성
    // (UserTalent.builder()를 통해 생성된 객체의 getTalentType()이 해당 TalentType을 반환한다고 가정)
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

  // --- updateUserPortfolioImages 테스트 ---
  @Test
  public void testUpdateUserPortfolioImages() {
    Long userId = 1L;
    // 간단한 portfolio Map (실제는 MultipartFile 또는 String 등 다양한 Object가 올 수 있음)
    Map<Integer, Object> portfolioMap = Map.of(1, "dummyString");
    UserUpdateDto dto = UserUpdateDto.builder()
        .portfolio(portfolioMap)
        .username("testUser")
        .build();

    // 기존에 저장된 portfolio가 없는 경우 빈 리스트 반환
    when(userPortfolioRepository.findAllByUserId(userId)).thenReturn(Collections.emptyList());

    // imageService.saveImagesS3WithSequence 호출 시 dummy UserPortfolioItem 리스트 반환
    UserPortfolioItem dummyItem = new UserPortfolioImg();
    List<UserPortfolioItem> dummyItems = List.of(dummyItem);
    when(imageService.saveImagesS3WithSequence(eq(portfolioMap), any(UserPortfolio.class),
        eq("testUser")))
        .thenReturn(dummyItems);

    // deleteAllByUserId와 saveAll은 단순히 호출되고, saveAll은 빈 리스트나 동일한 리스트 반환하도록 스텁 처리
    when(userPortfolioRepository.saveAll(any())).thenReturn(Collections.emptyList());

    UserPortfolio portfolio = adapter.updateUserPortfolioImages(userId, dto);

    assertNotNull(portfolio);
    // portfolio 내부에 addOrUpdatePortfolioItems() 호출로 추가되었는지 등은
    // UserPortfolio의 equals나 getter를 통해 상세하게 확인할 수 있다면 추가 검증 가능
  }

  // --- getUserPurposes 테스트 ---
  @Test
  public void testGetUserPurposes() {
    Long userId = 1L;
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

  // --- getUserTalents 테스트 ---
  @Test
  public void testGetUserTalents() {
    Long userId = 1L;
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

  // --- getUserPortfolioByUserId 테스트 ---
  @Test
  public void testGetUserPortfolioByUserId() {
    Long userId = 1L;
    UserPortfolioImg img1 = UserPortfolioImg.builder().build();
    UserPortfolioImg img2 = UserPortfolioImg.builder().build();
    List<UserPortfolioImg> imgs = List.of(img1, img2);
    when(userPortfolioRepository.findAllByUserId(userId)).thenReturn(imgs);

    List<UserPortfolioItem> result = adapter.getUserPortfolioByUserId(userId);

    assertNotNull(result);
    assertEquals(2, result.size());
  }
}
