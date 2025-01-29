package org.kiru.user.user.service.out;

import java.util.List;
import org.kiru.core.user.talent.domain.Talent.TalentType;
import org.kiru.core.user.user.entity.UserJpaEntity;
import org.kiru.core.user.userPortfolioItem.domain.UserPortfolio;
import org.kiru.core.user.userPurpose.domain.PurposeType;
import org.kiru.user.user.dto.request.UserUpdateDto;
import org.kiru.user.user.dto.request.UserUpdatePwdDto;


public interface UserUpdatePort {
    List<PurposeType> updateUserPurposes(Long userId, UserUpdateDto userUpdateDto);

    List<TalentType> updateUserTalents(Long userId, UserUpdateDto userUpdateDto);

    UserPortfolio updateUserPortfolioImages(Long userId, UserUpdateDto userUpdateDto);

    Boolean updateUserPwd(UserJpaEntity existingUser, UserUpdatePwdDto userUpdatePwdDto);
}
