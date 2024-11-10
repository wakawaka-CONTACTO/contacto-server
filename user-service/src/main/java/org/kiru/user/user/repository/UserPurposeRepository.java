package org.kiru.user.user.repository;

import java.util.List;
import org.kiru.core.user.userPurpose.entity.UserPurpose;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPurposeRepository extends JpaRepository<UserPurpose, Long> {
    List<UserPurpose> findAllByUserId(Long userId);
}
