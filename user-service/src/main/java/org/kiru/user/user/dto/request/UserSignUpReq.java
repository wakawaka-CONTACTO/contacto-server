package org.kiru.user.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import org.kiru.core.user.domain.LoginType;


public record UserSignUpReq(
        @NotNull
        String name,
        @NotNull
        LoginType loginType,
        @NotNull
        String socialId,
        @NotNull
        @Email
        String email,
        @NotNull
        String description,
        @NotNull
        String instagramId,
        @NotNull
        String webUrl
) {
}
