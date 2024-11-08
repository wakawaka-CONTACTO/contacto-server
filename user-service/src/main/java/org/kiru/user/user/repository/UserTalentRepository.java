package org.kiru.user.user.repository;

import java.util.List;
import org.kiru.core.talent.entity.UserTalent;
import org.kiru.core.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTalentRepository extends JpaRepository<UserTalent, Long> {
    List<UserTalent> findAllByUserId(Long userId);
}
