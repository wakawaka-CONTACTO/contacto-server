package org.kiru.user.portfolio.service.out;

import java.util.List;
import org.kiru.core.user.userPurpose.domain.PurposeType;
import org.kiru.user.portfolio.service.dto.PurposeList;
import org.kiru.user.userlike.dto.Longs;
import org.springframework.data.domain.Pageable;

public interface UserPurposePort {
    Longs getMatchingUserIdsByPurpose(List<PurposeType> purposeTypes, Pageable pageable);
    PurposeList findAllPurposeTypeByUserId(Long userId);
}
