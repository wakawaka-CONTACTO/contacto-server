package org.kiru.user.user.dto.request;

import lombok.Getter;

public record LogoutReq(
        String deviceId
) {
}
