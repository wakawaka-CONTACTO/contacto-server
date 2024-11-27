package org.kiru.user.user.service.out;

import java.util.List;
import org.kiru.core.user.talent.entity.UserTalent;
import org.kiru.core.user.user.domain.User;
import org.kiru.core.user.user.entity.UserJpaEntity;
import org.kiru.core.user.userPortfolioImg.entity.UserPortfolioImg;
import org.kiru.core.user.userPurpose.entity.UserPurpose;
import org.kiru.user.user.dto.request.UserUpdateDto;
import org.kiru.user.user.dto.request.UserUpdatePwdDto;

public interface UserUpdateUseCase {
    List<UserPurpose> updateUserPurposes(Long userId, UserUpdateDto userUpdateDto);

    List<UserTalent> updateUserTalents(Long userId, UserUpdateDto userUpdateDto);

    List<UserPortfolioImg> updateUserPortfolioImages(Long userId, UserUpdateDto userUpdateDto);

    User updateUserPwd(UserJpaEntity existingUser, UserUpdatePwdDto userUpdatePwdDto);
}
