package org.kiru.user.admin.service.out;

import java.util.List;
import org.kiru.user.admin.dto.AdminLikeUserResponse.AdminLikeUserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.kiru.user.admin.dto.AdminUserDto.UserDto;

public interface AdminUserQuery {
    Page<UserDto> findAll(Pageable pageable);

    List<UserDto> findUserByName(String name);

    Page<AdminLikeUserDto> findUserLikes(Pageable pageable, Long userId);

    Page<AdminLikeUserDto> findUserLiked(Pageable pageable, Long userId);

    Page<AdminLikeUserDto> findUserLikesByName(Pageable pageable, Long userId, String name);

    Page<AdminLikeUserDto> findUserLikedByName(Pageable pageable, Long userId, String name);
}