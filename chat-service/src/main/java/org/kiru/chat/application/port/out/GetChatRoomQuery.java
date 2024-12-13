package org.kiru.chat.application.port.out;

import java.util.List;
import java.util.Optional;
import org.kiru.core.chat.chatroom.domain.ChatRoom;
import org.springframework.data.domain.Pageable;

public interface GetChatRoomQuery {
    Optional<ChatRoom> findById(Long id, Boolean isUserAdmin);

    List<ChatRoom> findRoomsByUserId(Long userId, Pageable pageable);

    ChatRoom getOrCreateRoom(Long userId, Long adminId);

    ChatRoom findAndSetVisible(Long roomId);

    ChatRoom findRoomWithMessagesAndParticipants(Long roomId, Long userId, Boolean isUserAdmin);

}
