package org.kiru.user.portfolio.service.out;

import java.util.List;
import org.kiru.core.user.userPurpose.domain.PurposeType;
import org.springframework.data.domain.Pageable;

public interface GetUserPurposeQuery {
    List<Long> findUserIdByPurposeType(Long userId, List<PurposeType> purposeTypes, Pageable pageable);
}
