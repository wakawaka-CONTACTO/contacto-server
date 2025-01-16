package org.kiru.user.portfolio.service.out;

import java.util.List;
import org.springframework.data.domain.Pageable;

public interface GetRecommendUserIdsQuery {
    List<Long> getRecommendUserIds(Long userId, Pageable pageable);
}
