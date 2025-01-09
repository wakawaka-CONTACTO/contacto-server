package org.kiru.chat.adapter.out.persistence;

import jakarta.persistence.QueryHint;
import java.util.List;
import org.kiru.core.chat.message.entity.MessageJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<MessageJpaEntity, Long> {
    List<MessageJpaEntity> findAllByChatRoomIdOrderByCreatedAt(Long chatRoomId);
    List<MessageJpaEntity> findAllByChatRoomIdAndReadStatusFalse(Long chatRoomId);
    @QueryHints(value = {
            @QueryHint(name = "org.hibernate.readOnly", value = "true"),
            @QueryHint(name = "org.hibernate.fetchSize", value = "150"),
            @QueryHint(name = "jakarta.persistence.query.timeout", value = "5000")

    })
    Slice<MessageJpaEntity> findAllByChatRoomIdOrderByCreatedAt(Long chatRoomId, Pageable pageable);
}