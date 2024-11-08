package org.kiru.user.user.repository;

import java.util.List;
import java.util.Optional;
import javax.swing.text.html.Option;
import org.kiru.core.userPurpose.entity.UserPurpose;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPurposeRepository extends JpaRepository<UserPurpose, Long> {
    List<UserPurpose> findAllByUserId(Long userId);
}
