package org.kiru.user.user.service;

import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kiru.core.exception.EntityNotFoundException;
import org.kiru.core.exception.InvalidValueException;
import org.kiru.core.exception.UnauthorizedException;
import org.kiru.core.exception.code.FailureCode;
import org.kiru.core.user.user.domain.LoginType;
import org.kiru.core.user.user.domain.User;
import org.kiru.core.user.user.entity.UserJpaEntity;
import org.kiru.user.auth.jwt.JwtProvider;
import org.kiru.user.auth.jwt.Token;
import org.kiru.user.auth.jwt.refreshtoken.repository.RefreshTokenRepository;
import org.kiru.user.user.api.AlarmApiClient;
import org.kiru.user.user.dto.event.UserCreateEvent;
import org.kiru.user.user.dto.request.*;
import org.kiru.user.user.dto.response.CreatedDeviceRes;
import org.kiru.user.user.dto.response.SignHelpDtoRes;
import org.kiru.user.user.dto.response.UserJwtInfoRes;
import org.kiru.user.user.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

  @Transactional
  public UserJwtInfoRes signUp(UserSignUpReq req, List<MultipartFile> images,
      List<UserPurposesReq> purposes, List<UserTalentsReq> talents) {

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

    if(req.deviceToken() != null){
      saveDeviceToken(userEntity.getId(), req.deviceToken(), req.deviceType(), req.deviceId());
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

    if(req.deviceToken() != null){
      saveDeviceToken(user.getId(), req.deviceToken(), req.deviceType(), req.deviceId());
    }

    return UserJwtInfoRes.of(user.getId(), issuedToken.accessToken(), issuedToken.refreshToken());
  }

  /**
   * 토큰 재발급 처리.
   */
  @Transactional
  public UserJwtInfoRes reissue(final Long userId) {
    Date now = new Date();
    UserJpaEntity user = userRepository.findById(userId)
        .orElseThrow(() -> new EntityNotFoundException(FailureCode.USER_NOT_FOUND));

    refreshTokenRepository.deleteRefreshTokenByUserId(userId);
    Token newToken = jwtProvider.issueToken(userId, user.getEmail(), now);
    return UserJwtInfoRes.of(userId, newToken.accessToken(), newToken.refreshToken());
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

  private void saveDeviceToken(Long userId, String deviceToken, String deviceType, String deviceId) {
    CreatedDeviceReq createdDeviceReq = CreatedDeviceReq.of(userId, deviceToken, deviceType, deviceId);
    CreatedDeviceRes res = alarmApiClient.createDevice(createdDeviceReq);
    if (res.deviceTokenId() == -1) {
      log.info("😭이미 존재하는 디바이스 토큰 입니다 ");
    }else{
      log.info("😃성공적으로 디바이스 토큰을 저장했습니다. ");
    }
  }
}
