package org.kiru.user.user.repository;

import java.util.List;
import org.kiru.core.user.userPurpose.domain.PurposeType;
import org.kiru.core.user.userPurpose.entity.UserPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserPurposeRepository extends JpaRepository<UserPurpose, Long> {
    List<UserPurpose> findAllByUserId(Long userId);

    @Query("SELECT DISTINCT up.userId FROM UserPurpose up WHERE up.purposeType IN :purposes")
    List<Long> findUserIdsByPurposeTypes(@Param("purposes") List<PurposeType> purposes);

    void deleteAllByUserId(Long userId);
}
