package org.kiru.user.user.repository;

import java.util.List;
import org.kiru.core.user.talent.entity.UserTalent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTalentRepository extends JpaRepository<UserTalent, Long> {
    List<UserTalent> findAllByUserId(Long userId);
}
