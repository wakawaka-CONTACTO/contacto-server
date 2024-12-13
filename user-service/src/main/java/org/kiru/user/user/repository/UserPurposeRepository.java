package org.kiru.user.user.repository;

import java.util.List;
import org.kiru.core.user.userPurpose.domain.PurposeType;
import org.kiru.core.user.userPurpose.entity.UserPurpose;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPurposeRepository extends JpaRepository<UserPurpose, Long> {
    @Cacheable(value = "userPurpose", key = "#userId")
    List<UserPurpose> findAllByUserId(Long userId);

    @Query("SELECT up.userId "
            + "FROM UserPurpose up "
            + "WHERE up.purposeType IN :purposes "
            + "GROUP BY up.userId "
            + "ORDER BY COUNT(up.purposeType) DESC "
    )
    Slice<Long> findUserIdsByPurposeTypesOrderByCount(@Param("purposes") List<PurposeType> purposes, Pageable pageable);

    void deleteAllByUserId(Long userId);

    @Query("SELECT DISTINCT up.userId, COUNT(up2.purposeType) as purposeCount, " +
        "(SELECT COUNT(ul2) FROM UserLike ul2 WHERE ul2.likedUserId = up.userId) as likeCount " +
        "FROM UserPurpose up " +
        "LEFT JOIN UserPurpose up2 ON up.purposeType = up2.purposeType AND up2.userId = :userId " +
        "WHERE up.userId != :userId " +
        "AND NOT EXISTS ( " +
        "    SELECT 1 FROM UserLike ul " +
        "    WHERE (ul.likedUserId = up.userId AND ul.userId = :userId) " +
        "    OR (ul.userId = up.userId AND ul.likedUserId = :userId AND ul.isMatched = true) " +
        ") " +
        "GROUP BY up.userId " +
        "ORDER BY purposeCount DESC, likeCount DESC")
        Slice<Object[]> findDistinctUserIdsWithCounts(@Param("userId") Long userId, Pageable pageable);
}