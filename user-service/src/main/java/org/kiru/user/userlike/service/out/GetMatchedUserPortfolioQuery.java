package org.kiru.user.userlike.service.out;

import java.util.List;
import org.kiru.user.portfolio.dto.res.UserPortfolioResDto;

public interface GetMatchedUserPortfolioQuery {
    List<UserPortfolioResDto> findByUserIds(List<Long> userIds);
}
