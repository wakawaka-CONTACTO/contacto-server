package org.kiru.user.admin.service.out;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.kiru.user.admin.dto.AdminUserDto.UserDto;

public interface AdminUserQuery {
    Page<UserDto> findAll(Pageable pageable);

    UserDto findUserByName(String name);
}