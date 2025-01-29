package org.kiru.user.portfolio.service.out;

import java.util.List;
import org.kiru.core.user.userPortfolioItem.domain.UserPortfolioItem;
import org.kiru.user.portfolio.dto.res.UserPortfolioResDto;

public interface GetUserPortfoliosQuery {
    List<UserPortfolioResDto> findAllPortfoliosByUserIds(List<Long> userIds);
    List<UserPortfolioItem> getUserPortfoliosWithMinSequence(List<Long> allParticipantIds);
}