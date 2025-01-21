package org.kiru.chat.adapter.out.persistence;

import org.kiru.core.chat.message.entity.TranslateMessageJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TranslateMessageRepository extends JpaRepository<TranslateMessageJpaEntity, Long> {
}