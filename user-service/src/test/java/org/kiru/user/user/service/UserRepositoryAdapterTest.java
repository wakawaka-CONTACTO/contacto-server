package org.kiru.user.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
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

  private Long nonExistentUserId;
  private User nonExistentUser;
  private Long existentUserId;
  private User existentUser;

  @BeforeEach
  public void setUp() {
    // userId 2L인 경우 존재하지 않는 유저
    nonExistentUserId = 2L;
    nonExistentUser = User.builder()
        .id(nonExistentUserId)
        .username("nonexistent")
        .email("nonexistent@example.com")
        .description("No such user")
        .instagramId("no_insta")
        .webUrl("http://no-url.com")
        .password("anyPassword")
        .build();

    // userId 1L인 경우 실제 존재하는 유저
    existentUserId = 1L;
    existentUser = User.builder()
        .id(existentUserId)
        .username("testUser")
        .email("test@example.com")
        .description("Test description")
        .instagramId("testInsta")
        .webUrl("http://example.com")
        .password("originalPassword")
        .build();
  }

  @Test
  public void testSaveExistUser_userNotFound_withNonExistentUser() {
    when(userRepository.existsById(nonExistentUserId)).thenReturn(false);
    assertThrows(EntityNotFoundException.class, () -> adapter.saveExistUser(nonExistentUser));
  }

  @Test
  public void testSaveExistUser_UserNotFound_withMinimalUser() {
    User minimalUser = User.builder().id(nonExistentUserId).build();
    when(userRepository.existsById(nonExistentUserId)).thenReturn(false);
    assertThrows(EntityNotFoundException.class, () -> adapter.saveExistUser(minimalUser));
  }

  @Test
  public void testSaveExistUser_PasswordNotCleared() {
    when(userRepository.existsById(existentUserId)).thenReturn(true);
    UserJpaEntity savedEntity = UserJpaEntity.of(existentUser);
    when(userRepository.save(any(UserJpaEntity.class))).thenReturn(savedEntity);

    User savedUser = adapter.saveExistUser(existentUser);

    assertNotNull(savedUser);
    assertEquals("originalPassword", savedUser.getPassword(),
        "DB에 저장된 User의 password 필드가 null이어서는 안됩니다.");
  }
}
