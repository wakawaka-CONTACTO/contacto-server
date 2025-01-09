package org.kiru.user.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kiru.core.exception.ContactoException;
import org.kiru.core.exception.EntityNotFoundException;
import org.kiru.core.exception.UnauthorizedException;
import org.kiru.core.user.refreshtoken.RefreshToken;
import org.kiru.core.user.user.domain.LoginType;
import org.kiru.core.user.user.entity.UserJpaEntity;
import org.kiru.user.auth.jwt.JwtProvider;
import org.kiru.user.auth.jwt.Token;
import org.kiru.user.auth.jwt.refreshtoken.repository.RefreshTokenRepository;
import org.kiru.user.user.dto.request.SignHelpDto;
import org.kiru.user.user.dto.request.UserSignInReq;
import org.kiru.user.user.dto.request.UserSignUpReq;
import org.kiru.user.user.dto.response.SignHelpDtoRes;
import org.kiru.user.user.dto.response.UserJwtInfoRes;
import org.kiru.user.user.repository.UserRepository;
import org.kiru.user.user.service.AuthService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserSignUpReq testSignUpReq;
    private UserJpaEntity testUser;
    private Token testToken;

    @BeforeEach
    void setUp() {
        testSignUpReq = new UserSignUpReq(
            "test@example.com",
            "password123",
            "Test User",
            "Test Description",
            "testuser",
            "http://example.com",
            "kiru.day",
                LoginType.LOCAL
        );

        testUser = UserJpaEntity.builder()
            .id(1L)
            .email("test@example.com")
            .password("encodedPassword")
            .username("Test User")
            .loginType(LoginType.LOCAL)
            .build();

        testToken = new Token("testAccessToken", "testRefreshToken");
    }

    @Test
    @DisplayName("회원가입 - 성공")
    void signUp_Success() {
        // Given
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.save(any())).thenReturn(testUser);
        when(jwtProvider.issueToken(anyLong(), anyString())).thenReturn(testToken);

        // When
        UserJwtInfoRes result = authService.signUp(testSignUpReq, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.accessToken()).isNotNull();
        verify(userRepository).save(any());
    }

    @Test
    @DisplayName("로그인 - 성공")
    void signIn_Success() {
        // Given
        UserSignInReq signInReq = new UserSignInReq("test@example.com", "password123");
        when(userRepository.findByEmail(signInReq.email())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(jwtProvider.issueToken(anyLong(), anyString())).thenReturn(testToken);

        // When
        UserJwtInfoRes result = authService.signIn(signInReq);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.accessToken()).isEqualTo("testAccessToken");
        verify(userRepository).findByEmail(signInReq.email());
    }

    @Test
    @DisplayName("로그인 - 잘못된 비밀번호")
    void signIn_InvalidPassword() {
        // Given
        UserSignInReq signInReq = new UserSignInReq("test@example.com", "wrongPassword");
        when(userRepository.findByEmail(signInReq.email())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        // When & Then
        assertThrows(UnauthorizedException.class, () -> authService.signIn(signInReq));
    }

    @Test
    @DisplayName("토큰 재발급 - 성공")
    void reissue_Success() {
        // Given
        when(refreshTokenRepository.deleteByUserId(1L)).thenReturn(Optional.of(new RefreshToken()));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(jwtProvider.issueToken(anyLong(), anyString())).thenReturn(testToken);

        // When
        UserJwtInfoRes result = authService.reissue(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.accessToken()).isEqualTo("testAccessToken");
        verify(refreshTokenRepository).deleteByUserId(1L);
    }

    @Test
    @DisplayName("아이디 찾기 - 성공")
    void signHelp_Success() {
        // Given
        SignHelpDto signHelpDto = new SignHelpDto("Test User");
        when(userRepository.findByUsername(signHelpDto.userName())).thenReturn(Optional.of("test@example.com"));

        // When
        SignHelpDtoRes result = authService.signHelp(signHelpDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDecodeEmail()).contains("*");
        verify(userRepository).findByUsername(signHelpDto.userName());
    }

    @Test
    @DisplayName("아이디 찾기 - 존재하지 않는 사용자")
    void signHelp_UserNotFound() {
        // Given
        SignHelpDto signHelpDto = new SignHelpDto("NonExistentUser");
        when(userRepository.findByUsername(signHelpDto.userName())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> authService.signHelp(signHelpDto));
    }

    @Test
    @DisplayName("토큰 재발급 - 리프레시 토큰 없음")
    void reissue_RefreshTokenNotFound() {
        // Given
        when(refreshTokenRepository.deleteByUserId(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UnauthorizedException.class, () -> authService.reissue(1L));
    }
}
