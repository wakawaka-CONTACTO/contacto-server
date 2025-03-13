package org.kiru.user.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kiru.core.exception.EntityNotFoundException;
import org.kiru.core.exception.UnauthorizedException;
import org.kiru.core.user.talent.domain.Talent.TalentType;
import org.kiru.core.user.user.domain.LoginType;
import org.kiru.core.user.user.domain.Nationality;
import org.kiru.core.user.user.entity.UserJpaEntity;
import org.kiru.core.user.userPurpose.domain.PurposeType;
import org.kiru.user.auth.jwt.JwtProvider;
import org.kiru.user.auth.jwt.Token;
import org.kiru.user.auth.jwt.refreshtoken.repository.RefreshTokenRepository;
import org.kiru.user.user.dto.event.UserCreateEvent;
import org.kiru.user.user.dto.request.SignHelpDto;
import org.kiru.user.user.dto.request.UserPurposesReq;
import org.kiru.user.user.dto.request.UserSignInReq;
import org.kiru.user.user.dto.request.UserSignUpReq;
import org.kiru.user.user.dto.request.UserTalentsReq;
import org.kiru.user.user.dto.response.SignHelpDtoRes;
import org.kiru.user.user.dto.response.UserJwtInfoRes;
import org.kiru.user.user.repository.UserRepository;
import org.kiru.user.user.service.AuthService;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

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
    private PasswordEncoder passwordEncoder;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private UserSignUpReq testSignUpReq;
    private UserJpaEntity testUser;
    private List<UserTalentsReq> talentTypeRequest;
    private List<UserPurposesReq> purposeTypeRequest;
    private Token testToken;
    private List<MultipartFile> images;

    @BeforeEach
    void setUp() {
        testSignUpReq = new UserSignUpReq(
            "test@example.com",
            "password123",
            "password123",
            Nationality.KR,
            "Test Description",
            "testuser",
            "http://example.com",
            LoginType.LOCAL
        );

        testUser = UserJpaEntity.builder()
            .id(1L)
            .email("test@example.com")
            .password("encodedPassword")
            .username("Test User")
            .nationality(Nationality.KR)
            .loginType(LoginType.LOCAL)
            .build();

        testToken = new Token("testAccessToken", "testRefreshToken");

        talentTypeRequest = List.of(new UserTalentsReq(TalentType.ARCHITECTURE), new UserTalentsReq(TalentType.ADVERTISING));
        purposeTypeRequest = List.of(new UserPurposesReq(PurposeType.ART_RESIDENCY), new UserPurposesReq(PurposeType.GROUP_EXHIBITION));
        images = List.of(
                new MockMultipartFile("image1", "image1.jpg", "image/jpeg", new byte[0]),
                new MockMultipartFile("image2", "image2.jpg", "image/jpeg", new byte[0]),
                new MockMultipartFile("image3", "image3.jpg", "image/jpeg", new byte[0])
        )
        ;
    }

    @Test
    @DisplayName("회원가입 - 성공")
    void signUp_Success() {
        // Given
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.save(any())).thenReturn(testUser);
        when(jwtProvider.issueToken(anyLong(), anyString(),any())).thenReturn(testToken);
        // When
        UserJwtInfoRes result = authService.signUp(testSignUpReq, images, purposeTypeRequest, talentTypeRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.accessToken()).isNotNull();
        verify(userRepository).save(any());
        // publishEvent 호출 검증
        ArgumentCaptor<UserCreateEvent> eventCaptor = ArgumentCaptor.forClass(UserCreateEvent.class);
        verify(applicationEventPublisher, times(1)).publishEvent(eventCaptor.capture());

        UserCreateEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent).isNotNull();
        assertThat(capturedEvent.userId()).isEqualTo(testUser.getId());
        assertThat(capturedEvent.userName()).isEqualTo(testUser.getUsername());
        assertThat(capturedEvent.images()).isEqualTo(images);
        assertThat(capturedEvent.purposes()).isEqualTo(purposeTypeRequest);
        assertThat(capturedEvent.talents()).isEqualTo(talentTypeRequest);
    }

    @Test
    @DisplayName("로그인 - 성공")
    void signIn_Success() {
        // Given
        UserSignInReq signInReq = new UserSignInReq("test@example.com", "password123");
        when(userRepository.findByEmail(signInReq.email())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(jwtProvider.issueToken(anyLong(), anyString(),any())).thenReturn(testToken);

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
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(refreshTokenRepository).deleteRefreshTokenByUserId(1L);
        when(jwtProvider.issueToken(anyLong(), anyString(),any())).thenReturn(testToken);

        // When
        UserJwtInfoRes result = authService.reissue(1L);

        // Then
        assertThat(result).isNotNull();
        verify(refreshTokenRepository).deleteRefreshTokenByUserId(1L);
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
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(refreshTokenRepository).deleteRefreshTokenByUserId(anyLong());
        when(jwtProvider.issueToken(anyLong(), anyString(),any())).thenReturn(testToken);
        authService.reissue(1L);
        // When & Then
        verify(refreshTokenRepository).deleteRefreshTokenByUserId(1L);
    }

    @Test
    @DisplayName("토큰 발급시 user를 찾고 토큰을 삭제하는지 확인 - 성공")
    void reissue_순서대로_메소드가_작동하는지_확인하는_테스트() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(refreshTokenRepository).deleteRefreshTokenByUserId(anyLong());
        when(jwtProvider.issueToken(anyLong(), anyString(),any())).thenReturn(testToken);

        authService.reissue(1L);

        InOrder inOrder = inOrder(userRepository, refreshTokenRepository);
        inOrder.verify(userRepository).findById(1L);
        inOrder.verify(refreshTokenRepository).deleteRefreshTokenByUserId(anyLong());
        // When & Then
    }
}
