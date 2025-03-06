package org.kiru.user.user.service;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kiru.core.exception.EntityNotFoundException;
import org.kiru.core.user.user.domain.User;
import org.kiru.core.user.user.entity.UserJpaEntity;
import org.kiru.user.user.adapter.UserRepositoryAdapter;
import org.kiru.user.user.repository.UserRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserRepositoryAdapterTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserRepositoryAdapter adapter;

  @Test
  public void testSaveExistUser_userNotFound() {
    // given
    Long userId = 2L;
    User inputUser = User.builder()
        .id(userId)
        .username("nonexistent")
        .email("nonexistent@example.com")
        .description("No such user")
        .instagramId("no_insta")
        .webUrl("http://no-url.com")
        .password("anyPassword")
        .build();

    // 존재하지 않는 유저로 가정하여 false 반환
    when(userRepository.existsById(userId)).thenReturn(false);

    // then
    assertThrows(EntityNotFoundException.class, () -> {
      adapter.saveExistUser(inputUser);
    });
  }

  @Test
  public void testSaveExistUser_UserNotFound() {
    // given: 존재하지 않는 유저
    Long userId = 2L;
    User user = User.builder().id(userId).build();

    // existsById가 false를 반환하도록 설정
    when(userRepository.existsById(userId)).thenReturn(false);

    // then: EntityNotFoundException 예외가 발생해야 함
    assertThrows(EntityNotFoundException.class, () -> adapter.saveExistUser(user));
  }

  @Test
  public void testSaveExistUser_PasswordNotCleared() {
    // given: 비밀번호를 포함한 User 객체
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

    // userRepository.existsById 가 true를 반환하도록 설정
    when(userRepository.existsById(userId)).thenReturn(true);

    // 저장할 때 UserJpaEntity.of(user)를 통해 변환한 엔티티를 반환하도록 설정
    UserJpaEntity savedEntity = UserJpaEntity.of(user);
    when(userRepository.save(any(UserJpaEntity.class))).thenReturn(savedEntity);

    // when: saveExistUser 호출
    User savedUser = adapter.saveExistUser(user);

    // then: 반환된 User의 password 필드가 원래 값과 동일해야 함
    assertNotNull(savedUser);
    assertEquals(originalPassword, savedUser.getPassword(),
        "DB에 저장된 User의 password 필드가 null이어서는 안됩니다.");
  }
}
