package org.kiru.user.admin.adapter;

import lombok.RequiredArgsConstructor;
import org.kiru.user.admin.dto.AdminUserDto.UserDto;
import org.kiru.user.admin.service.out.AdminUserQuery;
import org.kiru.user.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class AdminAdapter implements AdminUserQuery {
    private final UserRepository userRepository;

    @Override
    public Page<UserDto> findAll(Pageable pageable) {
        return userRepository.findSimpleUsers(pageable);
    }

    @Override
    public UserDto findUserByName(String name) {
        return userRepository.findSimpleUserByName(name);
    }
}