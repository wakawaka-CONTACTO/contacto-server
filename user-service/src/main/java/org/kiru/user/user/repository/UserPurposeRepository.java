package org.kiru.user.user.repository;

import jakarta.persistence.QueryHint;
import java.util.List;
import org.kiru.core.user.userPurpose.domain.PurposeType;
import org.kiru.core.user.userPurpose.entity.UserPurpose;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public interface UserPurposeRepository extends JpaRepository<UserPurpose, Long> {
    @Cacheable(value = "userPurpose", key = "#userId")
    @QueryHints(value = {
            @QueryHint(name = "org.hibernate.readOnly", value = "true"),
            @QueryHint(name = "org.hibernate.fetchSize", value = "150"),
            @QueryHint(name = "jakarta.persistence.query.timeout", value = "5000")

    })
    List<UserPurpose> findAllByUserId(Long userId);

    @Query("SELECT up.userId "
            + "FROM UserPurpose up "
            + "WHERE up.purposeType IN :purposes "
            + "GROUP BY up.userId "
            + "ORDER BY COUNT(up.purposeType) DESC "
    )
    @QueryHints(value = {
            @QueryHint(name = "org.hibernate.readOnly", value = "true"),
            @QueryHint(name = "org.hibernate.fetchSize", value = "150"),
            @QueryHint(name = "jakarta.persistence.query.timeout", value = "5000")

    })
    @Cacheable(value = "userPurpose", key = "#purposes", unless = "#result == null")
    Slice<Long> findUserIdsByPurposeTypesOrderByCount(@Param("purposes") List<PurposeType> purposes, Pageable pageable);

    void deleteAllByUserId(Long userId);
}