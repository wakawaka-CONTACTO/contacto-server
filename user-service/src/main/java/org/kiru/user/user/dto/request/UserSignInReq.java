package org.kiru.user.user.dto.request;

import org.kiru.core.user.domain.LoginType;

public record UserSignInReq(
        LoginType loginType
) {
}
