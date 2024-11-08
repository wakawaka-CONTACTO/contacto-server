package org.kiru.user.user.dto.request;

import java.util.List;
import org.kiru.core.userPurpose.domain.PurposeType;

public record UserPurposesReq(
        PurposeType purposeType
) {
}
