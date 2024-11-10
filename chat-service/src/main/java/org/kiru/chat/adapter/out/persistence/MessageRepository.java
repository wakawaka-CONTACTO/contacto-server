package org.kiru.chat.adapter.out.persistence;

import java.util.List;
import org.kiru.core.chat.message.entity.MessageJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<MessageJpaEntity, Long> {
    List<MessageJpaEntity> findAllByChatRoomIdOrderByCreatedAt(Long chatRoomId);
}