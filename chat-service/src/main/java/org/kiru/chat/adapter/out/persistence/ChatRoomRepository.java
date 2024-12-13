package org.kiru.chat.adapter.out.persistence;


import java.util.List;
import java.util.Optional;
import org.kiru.core.chat.chatroom.entity.ChatRoomJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoomJpaEntity, Long> {
    @Query("SELECT cr, m, ujcr.userId FROM ChatRoomJpaEntity cr " +
            "LEFT JOIN MessageJpaEntity m ON m.chatRoomId = cr.id " +
            "LEFT JOIN UserJoinChatRoom ujcr ON ujcr.chatRoomId = cr.id " +
            "WHERE cr.id = :roomId AND cr.visible = true")
    Optional<List<Object[]>> findRoomWithMessagesAndParticipants(@Param("roomId") Long roomId);

    @Query("SELECT cr, m, ujcr.userId FROM ChatRoomJpaEntity cr " +
            "LEFT JOIN MessageJpaEntity m ON m.chatRoomId = cr.id " +
            "LEFT JOIN UserJoinChatRoom ujcr ON ujcr.chatRoomId = cr.id " +
            "WHERE cr.id = :roomId")
    Optional<List<Object[]>> findRoomWithMessagesAndParticipantsByAdmin(@Param("roomId") Long roomId);
}