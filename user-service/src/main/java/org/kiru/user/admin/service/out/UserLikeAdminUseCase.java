package org.kiru.user.admin.service.out;

import java.util.List;
import java.util.Map;
import org.kiru.user.admin.dto.AdminLikeUserResponse.AdminLikeUserDto;
import org.kiru.user.admin.dto.MatchedUserResponse;
import org.springframework.data.domain.Pageable;

public interface UserLikeAdminUseCase {
    List<AdminLikeUserDto> findUserLikesInternal(Pageable pageable, Long userId, String name, boolean isLiked);

    Map<Long, MatchedUserResponse> findAllMatchedUserIdWithMatchedTime(Long userId, Pageable pageable);
}
