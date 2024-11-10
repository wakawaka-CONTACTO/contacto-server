package org.kiru.user.user.dto.request;

import org.kiru.core.user.userPurpose.domain.PurposeType;

public record UserPurposesReq(
        PurposeType purposeType
) {
}
