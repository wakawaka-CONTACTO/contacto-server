package org.kiru.chat.adapter.out.persistence;

import java.util.List;
import org.kiru.core.chat.chatroom.entity.ChatRoomJpaEntity;
import org.kiru.core.chat.userchatroom.entity.UserJoinChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserJoinChatRoomRepository extends JpaRepository<UserJoinChatRoom, Long> {
    @Query("SELECT cr FROM ChatRoomJpaEntity cr " +
            "JOIN UserJoinChatRoom uj ON cr.id = uj.chatRoomId " +
            "WHERE uj.userId = :userId")
    List<ChatRoomJpaEntity> findChatRoomsByUserId(@Param("userId") Long userId);
}
