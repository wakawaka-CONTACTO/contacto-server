package org.kiru.user.user.service;



import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kiru.core.refreshtoken.RefreshToken;
import org.kiru.core.user.domain.LoginType;
import org.kiru.core.user.entity.UserJpaEntity;
import org.kiru.user.auth.jwt.refreshtoken.repository.RefreshTokenRepository;
import org.kiru.core.user.domain.User;
import org.kiru.user.auth.jwt.JwtProvider;
import org.kiru.user.auth.jwt.Token;
import org.kiru.user.exception.EntityNotFoundException;
import org.kiru.user.exception.UnauthorizedException;
import org.kiru.user.exception.code.FailureCode;
import org.kiru.user.user.dto.event.UserCreateEvent;
import org.kiru.user.user.dto.request.UserPurposesReq;
import org.kiru.user.user.dto.request.UserSignInReq;
import org.kiru.user.user.dto.request.UserSignUpReq;
import org.kiru.user.user.dto.request.UserTalentsReq;
import org.kiru.user.user.dto.response.UserJwtInfoRes;
import org.kiru.user.user.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
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
//    회원가입
    @Transactional
    public UserJwtInfoRes signUp(UserSignUpReq userSignUpReq, List<MultipartFile> images, List<UserPurposesReq> purposes, List<UserTalentsReq> talents) {
        User newUser = User.builder()
                .description(userSignUpReq.description())
                .email(userSignUpReq.email())
                .instagramId(userSignUpReq.instagramId())
                .loginType(userSignUpReq.loginType())
                .username(userSignUpReq.name())
                .socialId(userSignUpReq.socialId())
                .webUrl(userSignUpReq.webUrl())
                .build();
        UserJpaEntity user = userRepository.save(UserJpaEntity.of(newUser));
        Token issuedToken = issueToken(user.getId());
        applicationEventPublisher.publishEvent(
                UserCreateEvent.builder().userId(user.getId()).images(images).purposes(purposes).talents(talents).build());
        return UserJwtInfoRes.of(user.getId(), issuedToken.accessToken(), issuedToken.refreshToken());
    }
// 로그인
    @Transactional
    public UserJwtInfoRes signIn(final String token, final UserSignInReq userSignInReq) {
        Long userId = jwtProvider.getUserIdFromSubject(token);
        Long foundUserId = getUserByIdAndLoginType(userSignInReq.loginType(), userId).getId();
        deleteRefreshToken(foundUserId);
        Token issuedToken = issueToken(foundUserId);
        return UserJwtInfoRes.of(foundUserId, issuedToken.accessToken(), issuedToken.refreshToken());
    }


    @Transactional
    public UserJwtInfoRes reissue(final String refreshToken) {
        RefreshToken foundRefreshToken = getRefreshTokenByToken(refreshToken);
        Long userId = foundRefreshToken.getUserId();
        deleteRefreshToken(userId);
        Token newToken = issueToken(userId);
        return UserJwtInfoRes.of(userId, newToken.accessToken(), newToken.refreshToken());
    }

    private RefreshToken getRefreshTokenByToken(final String refreshToken) {
        try {
            return refreshTokenRepository.findRefreshTokenByToken(refreshToken)
                    .orElseThrow(() -> new UnauthorizedException(FailureCode.UNAUTHORIZED));
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            throw new UnauthorizedException(FailureCode.INVALID_REFRESH_TOKEN_VALUE);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new UnauthorizedException((FailureCode.UNAUTHORIZED));
        }
    }

    //토큰 발급
    private Token issueToken(final Long userId) {
        return jwtProvider.issueToken(userId);
    }

    //리프레시 토큰 삭제
    private void deleteRefreshToken(final Long userId) {
        refreshTokenRepository.deleteRefreshTokenByUserId(userId);
    }

    private UserJpaEntity getUserByIdAndLoginType(final LoginType loginType, final Long userId) {
        return userRepository.findByIdAndSocialId(userId, loginType.toString())
                .orElseThrow(() -> new EntityNotFoundException(FailureCode.USER_NOT_FOUND)
                );
    }
}
