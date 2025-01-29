package org.kiru.user.user.dto.request;

import lombok.Builder;
import org.kiru.core.user.talent.domain.Talent.TalentType;

@Builder
public record UserTalentsReq(
        TalentType talentType
        ) {
}
