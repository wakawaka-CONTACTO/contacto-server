package org.kiru.chat.adapter.out.persistence;

import jakarta.persistence.QueryHint;
import java.util.List;
import org.kiru.chat.adapter.out.persistence.dto.MessageWithTranslationDto;
import org.kiru.core.chat.message.entity.MessageJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface MessageRepository extends JpaRepository<MessageJpaEntity, Long> {
    List<MessageJpaEntity> findAllByChatRoomIdOrderByCreatedAt(Long chatRoomId);

    List<MessageJpaEntity> findAllByChatRoomIdAndReadStatusFalse(Long chatRoomId);

    @QueryHints(value = {
            @QueryHint(name = "org.hibernate.readOnly", value = "true"),
            @QueryHint(name = "org.hibernate.fetchSize", value = "150"),
            @QueryHint(name = "jakarta.persistence.query.timeout", value = "5000")

    })
    Slice<MessageJpaEntity> findAllByChatRoomId(Long chatRoomId, Pageable pageable);

    @Query(value = "SELECT m as message,mt as translateMessage FROM MessageJpaEntity m "
            + "LEFT JOIN TranslateMessageJpaEntity mt "
            + "ON m.id = mt.messageId "
            + "WHERE m.id IN :messageIds")
    List<MessageWithTranslationDto> findAllByMessageIds(List<Long> messageIds);

    List<MessageJpaEntity> findAllByChatRoomIdAndReadStatusFalseAndSenderIdNot(Long chatRoomId, Long userId);

    @Query("SELECT COUNT(m) FROM MessageJpaEntity m WHERE m.chatRoomId = :chatRoomId AND m.readStatus = false AND m.senderId <> :userId")
    int countUnreadMessagesByChatRoomIdAndUserId(Long chatRoomId, Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE MessageJpaEntity m SET m.readStatus = true WHERE m.chatRoomId = :chatRoomId AND m.readStatus = false AND m.senderId <> :userId")
    int markMessagesAsRead(Long chatRoomId, Long userId);
}