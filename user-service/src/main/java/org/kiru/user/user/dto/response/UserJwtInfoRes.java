package org.kiru.user.user.dto.response;

import lombok.Builder;

@Builder
public record UserJwtInfoRes(
        Long userId,
        String accessToken,
        String refreshToken
) {
    public static UserJwtInfoRes of(final Long userId, final String accessToken, final String refreshToken) {
        return UserJwtInfoRes.builder()
                .userId(userId)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
