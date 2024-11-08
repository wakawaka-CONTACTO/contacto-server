package org.kiru.user.user.dto.request;

import org.kiru.core.talent.domain.Talent.TalentType;

public record UserTalentsReq(
        TalentType talentType
        ) {
}
