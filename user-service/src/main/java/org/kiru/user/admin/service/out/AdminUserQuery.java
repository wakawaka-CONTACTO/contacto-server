package org.kiru.user.admin.service.out;

import java.util.List;
import org.kiru.user.admin.dto.AdminLikeUserResponse.AdminLikeUserDto;
import org.kiru.user.admin.dto.AdminUserDto.UserDto;
import org.springframework.data.domain.Pageable;

public interface AdminUserQuery {
    List<UserDto> findAll(Pageable pageable);

    List<UserDto> findUserByName(String name);

    List<AdminLikeUserDto> findUserLikes(Pageable pageable, Long userId);

    List<AdminLikeUserDto> findUserLiked(Pageable pageable, Long userId);

    List<AdminLikeUserDto> findUserLikesByName(Pageable pageable, Long userId, String name);

    List<AdminLikeUserDto> findUserLikedByName(Pageable pageable, Long userId, String name);
}