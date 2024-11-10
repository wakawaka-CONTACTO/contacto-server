package org.kiru.user.user.dto.request;


public record UserSignInReq(
        String email,
        String password
) {
}
