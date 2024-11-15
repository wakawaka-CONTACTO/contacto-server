package org.kiru.user.portfolio.service.out;

import java.util.List;
import org.kiru.user.portfolio.dto.res.UserPortfolioResDto;

public interface GetUserPortfoliosQuery {
    List<UserPortfolioResDto> findAllPortfoliosByUserIds(List<Long> userIds);
}
zxcvxcadfdsafsda