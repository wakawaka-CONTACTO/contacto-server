package org.kiru.user.admin.adapter;

import java.util.List;
import org.kiru.user.admin.dto.AdminLikeUserResponse.AdminLikeUserDto;
import org.kiru.user.admin.dto.AdminUserDto.UserDto;
import org.kiru.user.admin.service.out.AdminUserQuery;
import org.kiru.user.admin.service.out.UserLikeAdminUseCase;
import org.kiru.user.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class AdminAdapter implements AdminUserQuery {
    private final UserRepository userRepository;
    private final UserLikeAdminUseCase userLikeAdminUseCase;

    public AdminAdapter(UserRepository userRepository,
                        @Qualifier("userLikeJpaAdapter")
                        UserLikeAdminUseCase userLikeAdminUseCase) {
        this.userRepository = userRepository;
        this.userLikeAdminUseCase = userLikeAdminUseCase;
    }

    @Override
    public List<UserDto> findAll(Pageable pageable) {
        return userRepository.findSimpleUsers(pageable).getContent();
    }

    @Override
    public List<UserDto> findUserByName(String name) {
        return userRepository.findSimpleUserByName(name);
    }

    private List<AdminLikeUserDto> findUserLikesInternal(Pageable pageable, Long userId, String name, boolean isLiked) {
        return userLikeAdminUseCase.findUserLikesInternal(pageable, userId, name, isLiked);
    }

    @Override
    public List<AdminLikeUserDto> findUserLikes(Pageable pageable, Long userId) {
        return findUserLikesInternal(pageable, userId, null, false);
    }

    @Override
    public List<AdminLikeUserDto> findUserLiked(Pageable pageable, Long userId) {
        return findUserLikesInternal(pageable, userId, null, true);
    }

    @Override
    public List<AdminLikeUserDto> findUserLikesByName(Pageable pageable, Long userId, String name) {
        return findUserLikesInternal(pageable, userId, name, false);
    }

    @Override
    public List<AdminLikeUserDto> findUserLikedByName(Pageable pageable, Long userId, String name) {
        return findUserLikesInternal(pageable, userId, name, true);
    }
}