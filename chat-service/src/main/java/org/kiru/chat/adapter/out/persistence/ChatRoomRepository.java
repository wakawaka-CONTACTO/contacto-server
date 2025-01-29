package org.kiru.chat.adapter.out.persistence;


import jakarta.persistence.QueryHint;
import java.util.List;
import java.util.Optional;
import org.kiru.chat.adapter.out.persistence.dto.ChatRoomWithDetails;
import org.kiru.core.chat.chatroom.entity.ChatRoomJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoomJpaEntity, Long> {
    @Query("SELECT new org.kiru.chat.adapter.out.persistence.dto.ChatRoomWithDetails(cr, m, ujcr.userId) " +
            "FROM ChatRoomJpaEntity cr " +
            "LEFT JOIN UserJoinChatRoom ujcr ON ujcr.chatRoomId = cr.id " +
            "LEFT JOIN MessageJpaEntity m ON m.chatRoomId = cr.id " +
            "WHERE cr.id = :roomId AND cr.visible = true")
    @QueryHints(value = {
            @QueryHint(name = "org.hibernate.fetchSize", value = "150"),
            @QueryHint(name = "jakarta.persistence.query.timeout", value = "5000")
    })
    Optional<List<ChatRoomWithDetails>> findRoomWithMessagesAndParticipants(@Param("roomId") Long roomId);

    @Query("SELECT new org.kiru.chat.adapter.out.persistence.dto.ChatRoomWithDetails(cr, m, ujcr.userId) FROM ChatRoomJpaEntity cr " +
            "LEFT JOIN UserJoinChatRoom ujcr ON ujcr.chatRoomId = cr.id " +
            "LEFT JOIN MessageJpaEntity m ON m.chatRoomId = cr.id " +
            "WHERE cr.id = :roomId")
    @QueryHints(value = {
            @QueryHint(name = "org.hibernate.readOnly", value = "true"),
            @QueryHint(name = "org.hibernate.fetchSize", value = "150"),
            @QueryHint(name = "jakarta.persistence.query.timeout", value = "5000")

    })
    Optional<List<ChatRoomWithDetails>> findRoomWithMessagesAndParticipantsByAdmin(@Param("roomId") Long roomId);
}