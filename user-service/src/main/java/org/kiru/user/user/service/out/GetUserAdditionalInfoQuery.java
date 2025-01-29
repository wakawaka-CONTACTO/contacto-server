package org.kiru.user.user.service.out;

import java.util.List;
import org.kiru.core.user.talent.domain.Talent.TalentType;
import org.kiru.core.user.userPortfolioItem.domain.UserPortfolioItem;
import org.kiru.core.user.userPurpose.domain.PurposeType;

public interface GetUserAdditionalInfoQuery {
    List<PurposeType> getUserPurposes(Long userId);
    List<TalentType> getUserTalents(Long userId);
    List<UserPortfolioItem> getUserPortfolioByUserId(Long userId);
}
