package org.kiru.user.user.repository;

import jakarta.persistence.QueryHint;
import java.util.List;
import java.util.Optional;
import org.kiru.core.user.user.entity.UserJpaEntity;
import org.kiru.user.admin.dto.AdminUserDto.UserDto;
import org.kiru.user.user.dto.UserIdUsername;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;


@Repository
public interface UserRepository extends JpaRepository<UserJpaEntity,Long> {
    @QueryHints(value = {
            @QueryHint(name = "org.hibernate.fetchSize", value = "150"),
            @QueryHint(name = "jakarta.persistence.query.timeout", value = "5000")

    })
    Optional<UserJpaEntity> findByEmail(String email);

    @Query("SELECT u.id as id, u.username as userName " +
            "FROM UserJpaEntity u " +
            "WHERE u.id IN :userIds")
    @QueryHints(value = {
            @QueryHint(name = "org.hibernate.readOnly", value = "true"),
            @QueryHint(name = "org.hibernate.fetchSize", value = "150"),
            @QueryHint(name = "jakarta.persistence.query.timeout", value = "5000")

    })
    List<UserIdUsername> findUsernamesByIds(List<Long> userIds);

    @Query("SELECT u.email " +
            "FROM UserJpaEntity u " +
            "WHERE u.username = :username")
    @QueryHints(value = {
            @QueryHint(name = "org.hibernate.readOnly", value = "true"),
            @QueryHint(name = "org.hibernate.fetchSize", value = "150"),
            @QueryHint(name = "jakarta.persistence.query.timeout", value = "5000")

    })
    Optional<String> findByUsername(String username);

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

    boolean existsByEmail(String email);
}
