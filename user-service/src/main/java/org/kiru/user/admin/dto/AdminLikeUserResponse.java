package org.kiru.user.admin.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AdminLikeUserResponse(
        List<AdminLikeUserDto> likeMe,
        List<AdminLikeUserDto> iLike
) {
    public static AdminLikeUserResponse of(
            List<AdminLikeUserDto> likeMe,
            List<AdminLikeUserDto> iLike
    ) {
        return new AdminLikeUserResponse(likeMe, iLike);
    }
    public record AdminLikeUserDto(
            Long userId,
            String name,
            String portfolioImageUrl,
            LocalDateTime likedAt
    ) {

    }
}

