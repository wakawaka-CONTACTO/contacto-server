package org.kiru.user.user.repository;

import jakarta.persistence.QueryHint;
import java.util.List;
import org.kiru.core.user.talent.entity.UserTalent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;

public interface UserTalentRepository extends JpaRepository<UserTalent, Long> {
    @QueryHints(value = {
            @QueryHint(name = "org.hibernate.readOnly", value = "true"),
            @QueryHint(name = "org.hibernate.fetchSize", value = "150"),
            @QueryHint(name = "jakarta.persistence.query.timeout", value = "5000")

    })
    List<UserTalent> findAllByUserId(Long userId);

    @QueryHints(value = {
            @QueryHint(name = "org.hibernate.readOnly", value = "true"),
            @QueryHint(name = "org.hibernate.fetchSize", value = "150"),
            @QueryHint(name = "jakarta.persistence.query.timeout", value = "5000")

    })
    void deleteAllByUserId(Long userId);
}
