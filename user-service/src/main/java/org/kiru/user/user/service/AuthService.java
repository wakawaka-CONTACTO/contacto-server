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
import org.kiru.user.user.dto.event.UserCreateEvent;
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

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    @Transactional
    public UserJwtInfoRes signUp(UserSignUpReq userSignUpReq, List<MultipartFile> images,
                                 List<UserPurposesReq> purposes, List<UserTalentsReq> talents) {
        User.UserBuilder newUserBuilder = User.builder()
                .description(userSignUpReq.description())
                .email(userSignUpReq.email())
                .instagramId(userSignUpReq.instagramId())
                .loginType(userSignUpReq.loginType())
                .username(userSignUpReq.name())
                .socialId(userSignUpReq.socialId())
                .webUrl(userSignUpReq.webUrl());
        if (userSignUpReq.loginType() == LoginType.LOCAL) {
            newUserBuilder.password(encodePassword(userSignUpReq.password()));
        }
        User newUser = newUserBuilder.build();
        UserJpaEntity user = userRepository.save(UserJpaEntity.of(newUser));
        Date now = new Date();
        Token issuedToken = jwtProvider.issueToken(user.getId(), user.getEmail(),now);
        applicationEventPublisher.publishEvent(
                UserCreateEvent.builder()
                        .userName(user.getUsername())
                        .userId(user.getId())
                        .images(images)
                        .purposes(purposes)
                        .talents(talents)
                        .build()
        );
        return UserJwtInfoRes.of(user.getId(), issuedToken.accessToken(), issuedToken.refreshToken());
    }

    // 로그인
    @Transactional
    public UserJwtInfoRes signIn(final UserSignInReq userSignInReq) {
        Date now = new Date();
        return userRepository.findByEmail(userSignInReq.email())
                .filter(user -> checkPassword(userSignInReq.password(), user.getPassword()))
                .map(user -> {
                    refreshTokenRepository.deleteByUserId(user.getId());
                    Token issuedToken = jwtProvider.issueToken(user.getId(), user.getEmail(),now);
                    return UserJwtInfoRes.of(user.getId(), issuedToken.accessToken(), issuedToken.refreshToken());
                })
                .orElseThrow(() -> new UnauthorizedException(FailureCode.INVALID_USER_CREDENTIALS));
    }

    @Transactional
    public UserJwtInfoRes reissue(final Long userId) {
        Date now = new Date();
        UserJpaEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(FailureCode.USER_NOT_FOUND));
        refreshTokenRepository.deleteByUserId(userId)
                .orElseThrow(() -> new UnauthorizedException(FailureCode.INVALID_REFRESH_TOKEN_VALUE));
        Token newToken = jwtProvider.issueToken(userId, user.getEmail(),now);
        return UserJwtInfoRes.of(userId, newToken.accessToken(), newToken.refreshToken());
    }

    private String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    // 비밀번호 비교 로직
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
        if (atIndex == -1 || atIndex == 0) {
            throw new IllegalArgumentException("Invalid email address");
        }
        String localPart = email.substring(0, atIndex);
        StringBuilder maskedLocalPart = new StringBuilder();
        int localLength = localPart.length();
        if (localLength <= 2) {
            maskedLocalPart.append(localPart.charAt(0)).append("*");
        } else {
            maskedLocalPart.append(localPart.charAt(0))
                    .append(localPart.charAt(1))
                    .append("*".repeat(localLength - 4))
                    .append(localPart.charAt(localLength - 2))
                    .append(localPart.charAt(localLength - 1));
        }
        String domainPart = email.substring(atIndex + 1);
        String[] domainParts = domainPart.split("\\.");
        StringBuilder maskedDomainPart = new StringBuilder();

        if (domainParts.length >= 2) {
            String domainName = domainParts[0];
            maskedDomainPart.append(domainName.charAt(0)) // 첫 글자 표시
                    .append("*".repeat(domainName.length() - 2)) // 중간 글자 마스킹
                    .append(domainName.charAt(domainName.length() - 1))
                    .append(".");
            maskedDomainPart.append("***");
        } else {
            throw new IllegalArgumentException("Invalid domain format");
        }
        return maskedLocalPart + "@" + maskedDomainPart;
    }
}
