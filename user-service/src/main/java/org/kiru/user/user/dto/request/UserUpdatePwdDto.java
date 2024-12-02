package org.kiru.user.user.dto.request;

public record UserUpdatePwdDto(
        String email,
        String password
) {
}