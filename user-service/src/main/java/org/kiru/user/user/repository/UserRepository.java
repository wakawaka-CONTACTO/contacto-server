package org.kiru.user.user.repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.hibernate.annotations.Cache;
import org.kiru.core.user.user.domain.User;
import org.kiru.core.user.user.entity.UserJpaEntity;
import org.kiru.user.admin.dto.AdminMatchedUserResponse;
import org.kiru.user.admin.dto.AdminUserDto;
import org.kiru.user.admin.dto.AdminUserDto.UserDto;
import org.kiru.user.admin.dto.MatchedUserResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserJpaEntity,Long> {
    Optional<UserJpaEntity> findByIdAndSocialId(Long userId, String socialID);

    Optional<UserJpaEntity> findByEmail(String email);

    @Query("SELECT u.id, u.username " +
            "FROM UserJpaEntity u " +
            "WHERE u.id IN :userIds")
    List<Object[]> findUsernamesByIds(List<Long> userIds);

    @Query("SELECT u.email " +
            "FROM UserJpaEntity u " +
            "WHERE u.username = :username")
    Optional<String> findByUsername(String username);

    @Query("SELECT u.username " +
            "FROM UserJpaEntity u " +
            "WHERE u.username = :username")
    Optional<String> findUserNameByUsername(String username);

    @Query("SELECT new org.kiru.user.admin.dto.AdminUserDto$UserDto(u.id, u.username, p.portfolioImageUrl) " +
            "FROM UserJpaEntity u " +
            "INNER JOIN UserPortfolioImg p ON u.id = p.userId " +
            "WHERE p.sequence = 1")
    Page<UserDto> findSimpleUsers(Pageable pageable);

    @Query("SELECT new org.kiru.user.admin.dto.AdminUserDto$UserDto(u.id, u.username, p.portfolioImageUrl) " +
            "FROM UserJpaEntity u " +
            "INNER JOIN UserPortfolioImg p ON u.id = p.userId " +
            "WHERE u.username LIKE %:name%")
    List<UserDto> findSimpleUserByName(String name);

    @Query("SELECT u.id, u.username " +
            "FROM UserJpaEntity u " +
            "WHERE u.id IN :userIds")
    List<Object> findSimpleUserByIds(List<Long> userIds);
}
