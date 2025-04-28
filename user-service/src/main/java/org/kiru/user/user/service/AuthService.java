package org.kiru.user.user.service;

import java.util.Date;
import java.util.List;

import org.kiru.core.exception.ConflictException;
import org.kiru.core.exception.EntityNotFoundException;
import org.kiru.core.exception.InvalidValueException;
import org.kiru.core.exception.UnauthorizedException;
import org.kiru.core.exception.code.FailureCode;
import org.kiru.core.jwt.AccessTokenGenerator;
import org.kiru.core.jwt.JwtTokenParser;
import org.kiru.core.user.refreshtoken.RefreshToken;
import org.kiru.core.user.user.domain.LoginType;
import org.kiru.core.user.user.domain.User;
import org.kiru.core.user.user.entity.UserJpaEntity;
import org.kiru.user.auth.jwt.JwtProvider;
import org.kiru.user.auth.jwt.Token;
import org.kiru.user.auth.jwt.refreshtoken.repository.RefreshTokenRepository;
import org.kiru.user.user.api.AlarmApiClient;
import org.kiru.user.user.dto.event.UserCreateEvent;
import org.kiru.user.user.dto.request.CreatedDeviceReq;
import org.kiru.user.user.dto.request.SignHelpDto;
import org.kiru.user.user.dto.request.UserPurposesReq;
import org.kiru.user.user.dto.request.UserSignInReq;
import org.kiru.user.user.dto.request.UserSignUpReq;
import org.kiru.user.user.dto.request.UserTalentsReq;
import org.kiru.user.user.dto.response.SignHelpDtoRes;
import org.kiru.user.user.dto.response.UserJwtInfoRes;
import org.kiru.user.user.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class AuthService {

  private final UserRepository userRepository;
  private final JwtProvider jwtProvider;
  private final AlarmApiClient alarmApiClient;
  private final RefreshTokenRepository refreshTokenRepository;
  private final ApplicationEventPublisher applicationEventPublisher;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenParser jwtTokenParser;
  private final AccessTokenGenerator accessTokenGenerator;

  @Transactional
  public UserJwtInfoRes signUp(UserSignUpReq req, List<MultipartFile> images,
      List<UserPurposesReq> purposes, List<UserTalentsReq> talents) {

    // 유저 이름 중복 체크
    if (userRepository.findByUsername(req.name()).isPresent()) {
      throw new ConflictException(FailureCode.DUPLICATE_NICKNAME);
    }

    User newUser = userBuilder(req);
    UserJpaEntity userEntity = userRepository.save(UserJpaEntity.of(newUser));

    Token issuedToken = jwtProvider.issueToken(userEntity.getId(), userEntity.getEmail(), new Date());

    applicationEventPublisher.publishEvent(
        UserCreateEvent.builder()
            .userName(userEntity.getUsername())
            .userId(userEntity.getId())
            .images(images)
            .purposes(purposes)
            .talents(talents)
            .build()
    );

    if(req.firebaseToken() != null){
      saveFirebaseToken(userEntity.getId(), req.firebaseToken(), req.deviceType(), req.deviceId());
    }

    return UserJwtInfoRes.of(userEntity.getId(), issuedToken.accessToken(), issuedToken.refreshToken());
  }

  private User userBuilder(UserSignUpReq req){
    User.UserBuilder userBuilder = User.builder()
        .description(req.description())
        .email(req.email())
        .instagramId(req.instagramId())
        .loginType(req.loginType())
        .username(req.name())
        .nationality(req.nationality())
        .webUrl(req.webUrl());

    if (req.loginType() == LoginType.LOCAL) {
      userBuilder.password(encodePassword(req.password()));
    }
    return userBuilder.build();
  }

  /**
   * 로그인 처리.
   */
  @Transactional
  public UserJwtInfoRes signIn(final UserSignInReq req) {
    Date now = new Date();
    UserJpaEntity user = userRepository.findByEmail(req.email())
        .orElseThrow(() -> new UnauthorizedException(FailureCode.INVALID_USER_CREDENTIALS));

    if (!passwordEncoder.matches(req.password(), user.getPassword())) {
      throw new UnauthorizedException(FailureCode.PASSWORD_MISMATCH);
    }

    refreshTokenRepository.deleteByUserId(user.getId());
    Token issuedToken = jwtProvider.issueToken(user.getId(), user.getEmail(), now);

    if(req.firebaseToken() != null){
      saveFirebaseToken(user.getId(), req.firebaseToken(), req.deviceType(), req.deviceId());
    }

    return UserJwtInfoRes.of(user.getId(), issuedToken.accessToken(), issuedToken.refreshToken());
  }

  /**
   * 토큰 재발급 처리.
   */
  public UserJwtInfoRes reissue(final String refreshToken) {
    try {
        // 리프레시 토큰 파싱 및 검증
        Jws<Claims> claims = jwtTokenParser.parseToken(refreshToken);
        Long userId = jwtTokenParser.getUserIdFromClaims(claims);
        String email = jwtTokenParser.getEmailFromClaims(claims);

        // 데이터베이스에서 리프레시 토큰 조회 및 검증
        RefreshToken storedRefreshToken = refreshTokenRepository.findByUserId(userId)
            .orElseThrow(() -> new UnauthorizedException(FailureCode.INVALID_REFRESH_TOKEN_VALUE));

        if (!storedRefreshToken.getToken().equals(refreshToken)) {
            throw new UnauthorizedException(FailureCode.INVALID_REFRESH_TOKEN_VALUE);
        }

        // 새로운 액세스 토큰 생성
        Date now = new Date();
        String newAccessToken = accessTokenGenerator.generateToken(userId, email, now);

        return UserJwtInfoRes.of(userId, newAccessToken, refreshToken);
    } catch (ExpiredJwtException e) {
        throw new UnauthorizedException(FailureCode.EXPIRED_REFRESH_TOKEN);
    } catch (Exception e) {
        log.error("Token reissue failed", e);
        throw new UnauthorizedException(FailureCode.INVALID_REFRESH_TOKEN_VALUE);
    }
  }

  private String encodePassword(String rawPassword) {
    return passwordEncoder.encode(rawPassword);
  }

  public boolean checkPassword(String rawPassword, String encodedPassword) {
    if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
      throw new UnauthorizedException(FailureCode.PASSWORD_MISMATCH);
    }
    return true;
  }

  public SignHelpDtoRes signHelp(SignHelpDto signHelpDto) {
    String email = userRepository.findByUsername(signHelpDto.userName())
        .orElseThrow(() -> new EntityNotFoundException(FailureCode.USER_NOT_FOUND));
    try {
      String maskedEmail = maskEmail(email);
      return new SignHelpDtoRes(maskedEmail);
    } catch (IllegalArgumentException e) {
      throw new InvalidValueException(FailureCode.INVALID_EMAIL_FORMAT);
    }
  }

  private String maskEmail(String email) {
    int atIndex = email.indexOf("@");
    if (atIndex <= 0) {
      throw new IllegalArgumentException("Invalid email address");
    }
    String localPart = email.substring(0, atIndex);
    String maskedLocalPart = maskLocalPart(localPart);

    String domainPart = email.substring(atIndex + 1);
    String maskedDomainPart = maskDomainPart(domainPart);

    return maskedLocalPart + "@" + maskedDomainPart;
  }

  private String maskLocalPart(String localPart) {
    int length = localPart.length();
    if (length <= 3) {
      return localPart.charAt(0) + "*";
    } else if (length == 4) {
      return localPart.charAt(0) + "*" + localPart.charAt(length - 1);
    } else {
      String middleMask = "*".repeat(Math.max(0, length - 4));
      return "" + localPart.charAt(0) + localPart.charAt(1) + middleMask
          + localPart.charAt(length - 2) + localPart.charAt(length - 1);
    }
  }

  private String maskDomainPart(String domainPart) {
    String[] parts = domainPart.split("\\.");
    if (parts.length < 2) {
      throw new IllegalArgumentException("Invalid domain format");
    }
    String domainName = parts[0];
    if (domainName.length() < 2) {
      throw new IllegalArgumentException("Invalid domain name");
    }
    String maskedDomainName = "" + domainName.charAt(0)
        + "*".repeat(Math.max(0, domainName.length() - 2))
        + domainName.charAt(domainName.length() - 1);
    return maskedDomainName + ".***";
  }

  private void saveFirebaseToken(Long userId, String firebaseToken, String deviceType, String deviceId) {
    CreatedDeviceReq createdDeviceReq = CreatedDeviceReq.of(userId, firebaseToken, deviceType, deviceId);
     alarmApiClient.createDevice(createdDeviceReq);
  }

    public void logout(Long userId, String deviceId) {
        // Firebase Token 삭제
        if (deviceId != null) {
            alarmApiClient.deleteDevice(userId, deviceId);
        }
    }
}
