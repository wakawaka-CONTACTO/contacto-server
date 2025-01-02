package org.kiru.user.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kiru.core.exception.EntityNotFoundException;
import org.kiru.core.user.talent.entity.UserTalent;
import org.kiru.core.user.user.domain.User;
import org.kiru.core.user.user.entity.UserJpaEntity;
import org.kiru.core.user.userPortfolioImg.entity.UserPortfolioImg;
import org.kiru.user.portfolio.repository.UserPortfolioRepository;
import org.kiru.user.user.dto.request.UserUpdateDto;
import org.kiru.user.user.dto.request.UserUpdatePwdDto;
import org.kiru.user.user.repository.UserPurposeRepository;
import org.kiru.user.user.repository.UserRepository;
import org.kiru.user.user.repository.UserTalentRepository;
import org.kiru.user.user.service.out.UserQueryWithCache;
import org.kiru.user.user.service.out.UserUpdateUseCase;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserQueryWithCache userQueryWithCache;

    @Mock
    private UserPurposeRepository userPurposeRepository;

    @Mock
    private UserTalentRepository userTalentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserPortfolioRepository userPortfolioRepository;

    @Mock
    private UserUpdateUseCase userUpdateUseCase;


    private UserJpaEntity testUserJpa;
    private User testUser;
    private UserUpdateDto testUpdateDto;
    private List<UserTalent> testTalents;
    private List<UserPortfolioImg> testPortfolioImgs;

    @BeforeEach
    void setUp() {
        testUserJpa = UserJpaEntity.builder()
                .id(1L)
                .email("test@example.com")
                .username("testUser")
                .description("Test Description")
                .build();
        testUser = User.of(testUserJpa);
        testUpdateDto = new UserUpdateDto();
        testUpdateDto.setUsername("updatedUser");
        testUpdateDto.setDescription("Updated Description");
        testUpdateDto.setEmail("updated@example.com");

        testTalents = Arrays.asList(
            UserTalent.builder().id(1L).userId(1L).build(),
            UserTalent.builder().id(2L).userId(1L).build()
        );

        testPortfolioImgs = Arrays.asList(
            UserPortfolioImg.builder().id(1L).userId(1L).sequence(1).portfolioImageUrl("url1").build(),
            UserPortfolioImg.builder().id(2L).userId(1L).sequence(2).portfolioImageUrl("url2").build()
        );
    }

    @Test
    @DisplayName("사용자 상세 정보 조회 - 성공")
    void getUserDetails_Success() {
        // Given
        when(userQueryWithCache.getUser(1L)).thenReturn(testUser);
        when(userPurposeRepository.findAllByUserId(1L)).thenReturn(Arrays.asList());
        when(userTalentRepository.findAllByUserId(1L)).thenReturn(testTalents);
        when(userPortfolioRepository.findAllByUserId(1L)).thenReturn(testPortfolioImgs);

        // When
        User result = userService.getUserFromIdToMainPage(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(userQueryWithCache).getUser(1L);
    }

    @Test
    @DisplayName("사용자 정보 수정 - 성공")
    void updateUser_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUserJpa));
        when(userQueryWithCache.saveUser(any())).thenReturn(testUser);
        when(userUpdateUseCase.updateUserTalents(eq(1L), any())).thenReturn(testTalents);
        when(userUpdateUseCase.updateUserPortfolioImages(eq(1L), any())).thenReturn(testPortfolioImgs);
        when(userTalentRepository.saveAll(any())).thenReturn(testTalents);
        when(userPortfolioRepository.saveAll(any())).thenReturn(testPortfolioImgs);

        // When
        User result = userService.updateUser(1L, testUpdateDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("updated@example.com");
        verify(userRepository).findById(1L);
        verify(userQueryWithCache).saveUser(any());
    }

    @Test
    @DisplayName("사용자 정보 수정 - 존재하지 않는 사용자")
    void updateUser_UserNotFound() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        // When & Then
        assertThrows(EntityNotFoundException.class, () -> userService.updateUser(999L, testUpdateDto));
        verify(userRepository).findById(999L);
    }

    @Test
    @DisplayName("비밀번호 변경 - 성공")
    void updateUserPwd_Success() {
        // Given
        UserUpdatePwdDto pwdDto = new UserUpdatePwdDto("test@example.com", "newPassword");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUserJpa));
        when(userUpdateUseCase.updateUserPwd(any(), any())).thenReturn(true);
        // When
        Boolean result = userService.updateUserPwd(pwdDto);
        // Then
        assertThat(result).isTrue();
        verify(userRepository).findByEmail("test@example.com");
        verify(userUpdateUseCase).updateUserPwd(any(), any());
    }

    @Test
    @DisplayName("회원 탈퇴 - 성공")
    void deleteUser_Success() {
        // Given
        Long userId = 1L;

        // When
        userService.deleteUser(userId);

        // Then
        verify(userRepository).deleteById(userId);
    }
}
