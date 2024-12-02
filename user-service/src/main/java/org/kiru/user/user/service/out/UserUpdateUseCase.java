package org.kiru.user.user.service.out;

import java.util.List;
import org.kiru.core.user.talent.entity.UserTalent;
import org.kiru.core.user.user.entity.UserJpaEntity;
import org.kiru.core.user.userPortfolioImg.entity.UserPortfolioImg;
import org.kiru.core.user.userPurpose.entity.UserPurpose;
import org.kiru.user.user.dto.request.UserUpdateDto;
import org.kiru.user.user.dto.request.UserUpdatePwdDto;
import org.kiru.user.user.dto.response.UpdatePwdResponse;
import software.amazon.awssdk.services.s3.endpoints.internal.Value.Bool;

public interface UserUpdateUseCase {
    List<UserPurpose> updateUserPurposes(Long userId, UserUpdateDto userUpdateDto);

    List<UserTalent> updateUserTalents(Long userId, UserUpdateDto userUpdateDto);

    List<UserPortfolioImg> updateUserPortfolioImages(Long userId, UserUpdateDto userUpdateDto);

    Boolean updateUserPwd(UserJpaEntity existingUser, UserUpdatePwdDto userUpdatePwdDto);
}
