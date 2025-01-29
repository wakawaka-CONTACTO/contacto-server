package org.kiru.user.user.event;

import lombok.RequiredArgsConstructor;
import org.kiru.core.user.refreshtoken.RefreshToken;
import org.kiru.user.auth.jwt.refreshtoken.repository.RefreshTokenRepository;
import org.kiru.user.user.dto.event.TokenCreateEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Service
public class UserTokenEventService {
    private final RefreshTokenRepository refreshTokenRepository;

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void userTalentsCreate(TokenCreateEvent tokenEvent){
        RefreshToken refreshToken = RefreshToken.builder()
                .userId(tokenEvent.userId())
                .token(tokenEvent.token())
                .expiredAt(tokenEvent.expiredAt())
                .build();
        refreshTokenRepository.save(refreshToken);
    }
}
