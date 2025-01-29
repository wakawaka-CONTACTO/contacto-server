package org.kiru.user.portfolio.service.out;

import java.util.List;
import org.kiru.core.user.userPortfolioItem.domain.UserPortfolioItem;

public interface SaveUserPortfolioPort {
    void saveAll(List<UserPortfolioItem> userPortfolioItems);
    void save(UserPortfolioItem newImage);
}
