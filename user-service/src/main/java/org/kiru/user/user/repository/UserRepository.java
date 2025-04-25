package org.kiru.user.user.repository;

import java.util.List;
import java.util.Optional;

import org.kiru.core.user.user.entity.UserJpaEntity;
import org.kiru.user.admin.dto.AdminUserDto.UserDto;
import org.kiru.user.user.dto.UserIdUsername;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.QueryHint;


@Repository
public interface UserRepository extends JpaRepository<UserJpaEntity,Long> {
    @QueryHints(value = {
            @QueryHint(name = "org.hibernate.fetchSize", value = "150"),
            @QueryHint(name = "jakarta.persistence.query.timeout", value = "5000")

    })
    Optional<UserJpaEntity> findByEmail(String email);

    @QueryHints(value = {
            @QueryHint(name = "org.hibernate.readOnly", value = "true"),
            @QueryHint(name = "org.hibernate.fetchSize", value = "150"),
            @QueryHint(name = "jakarta.persistence.query.timeout", value = "5000")

    })
    @Query("SELECT u.id as id, u.username as username " +
            "FROM UserJpaEntity u " +
            "WHERE u.id IN :userIds")
    List<UserIdUsername> findUsernamesByIds(List<Long> userIds);

    @Query("SELECT u.email " +
            "FROM UserJpaEntity u " +
            "WHERE LOWER(u.username) = LOWER(:username)")
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
            "WHERE p.sequence = 1 AND u.username LIKE %:name% ")
    Slice<UserDto> findSimpleUserByName(String name, Pageable pageable);

    @Query(
            value = """
        WITH my_purposes AS (
            SELECT purpose_type
            FROM user_purposes
            WHERE user_id = :userId
        ),
        excluded_users AS (
            SELECT blocked_user_id AS user_id FROM user_block WHERE user_id = :userId
            UNION
            SELECT user_id FROM user_block WHERE blocked_user_id = :userId
            UNION
            SELECT liked_user_id FROM user_like
            WHERE user_id = :userId AND like_status = 'LIKE'
        ),
        purpose_match AS (
            SELECT up.user_id, COUNT(*) AS purpose_match_count
            FROM user_purposes up
            JOIN my_purposes mp ON up.purpose_type = mp.purpose_type
            WHERE up.user_id != :userId
            GROUP BY up.user_id
        ),
        liked_me AS (
            SELECT DISTINCT user_id
            FROM user_like
            WHERE liked_user_id = :userId AND like_status = 'LIKE'
        ),
        like_received_count AS (
            SELECT liked_user_id AS user_id, COUNT(*) AS like_received_count
            FROM user_like
            WHERE like_status = 'LIKE'
            GROUP BY liked_user_id
        ),
        disliked_users AS (
            SELECT liked_user_id AS user_id, updated_at
            FROM user_like
            WHERE user_id = :userId AND like_status = 'DISLIKE'
        )

        SELECT u.id
        FROM users u
        LEFT JOIN purpose_match pm ON u.id = pm.user_id
        LEFT JOIN liked_me lm ON u.id = lm.user_id
        LEFT JOIN like_received_count lrc ON u.id = lrc.user_id
        LEFT JOIN disliked_users du ON u.id = du.user_id
        WHERE u.id != :userId
          AND u.id NOT IN (SELECT user_id FROM excluded_users)
          AND u.deleted = false
        ORDER BY
            CASE WHEN du.user_id IS NULL THEN 0 ELSE 1 END ASC,
            COALESCE(pm.purpose_match_count, 0) DESC,
            CASE WHEN lm.user_id IS NOT NULL THEN 1 ELSE 0 END DESC,
            COALESCE(lrc.like_received_count, 0) DESC,
            du.updated_at ASC NULLS LAST
        LIMIT :limit OFFSET :offset
        """,
            nativeQuery = true
    )
    @QueryHints(value = {
            @QueryHint(name = "org.hibernate.readOnly", value = "true"),
            @QueryHint(name = "org.hibernate.fetchSize", value = "150"),
            @QueryHint(name = "jakarta.persistence.query.timeout", value = "5000")
    })
    List<Long> findRecommendedUserIds(@Param("userId") Long userId,
                                      @Param("limit") int limit,
                                      @Param("offset") int offset);
}
