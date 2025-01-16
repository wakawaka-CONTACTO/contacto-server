package org.kiru.user.admin.service.out;

import java.util.List;
import org.kiru.user.admin.dto.AdminLikeUserResponse.AdminLikeUserDto;
import org.springframework.data.domain.Pageable;

public interface UserLikeAdminUseCase {
    List<AdminLikeUserDto> findUserLikesInternal(Pageable pageable, Long userId, String name, boolean isLiked);
}
