package org.kiru.chat.adapter.out.persistence;

import jakarta.persistence.QueryHint;
import java.util.List;
import org.kiru.chat.adapter.out.persistence.dto.MessageWithTranslationDto;
import org.kiru.core.chat.message.entity.MessageJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<MessageJpaEntity, Long> {
    List<MessageJpaEntity> findAllByChatRoomIdOrderByCreatedAt(Long chatRoomId);
    List<MessageJpaEntity> findAllByChatRoomIdAndReadStatusFalse(Long chatRoomId);
    /**
     * Retrieves a paginated slice of messages for a specific chat room, ordered by creation timestamp.
     *
     * @param chatRoomId The unique identifier of the chat room to retrieve messages from
     * @param pageable   Pagination and sorting information for the query
     * @return A slice of message entities, optimized for performance with read-only, limited fetch size, and timeout configurations
     *
     * @see Slice
     * @see Pageable
     */
    @QueryHints(value = {
            @QueryHint(name = "org.hibernate.readOnly", value = "true"),
            @QueryHint(name = "org.hibernate.fetchSize", value = "150"),
            @QueryHint(name = "jakarta.persistence.query.timeout", value = "5000")

    })
    Slice<MessageJpaEntity> findAllByChatRoomIdOrderByCreatedAt(Long chatRoomId, Pageable pageable);

    /**
     * Retrieves messages with their optional translations for a given list of message IDs.
     *
     * @param messageIds A list of message identifiers to fetch
     * @return A list of {@link MessageWithTranslationDto} containing messages and their corresponding translations
     * @throws IllegalArgumentException if the messageIds list is null or empty
     */
    @Query(value = "SELECT m as message,mt as translateMessage FROM MessageJpaEntity m "
            + "LEFT JOIN TranslateMessageJpaEntity mt "
            + "ON m.id = mt.messageId "
            + "WHERE m.id IN :messageIds")
    List<MessageWithTranslationDto> findAllByMessageIds(List<Long> messageIds);
}