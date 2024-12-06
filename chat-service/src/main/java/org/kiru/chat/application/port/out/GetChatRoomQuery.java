package org.kiru.chat.application.port.out;

import java.util.List;
import java.util.Optional;
import org.kiru.core.chat.chatroom.domain.ChatRoom;

public interface GetChatRoomQuery {
    Optional<ChatRoom> findById(Long id);

    List<ChatRoom> findRoomsByUserId(Long userId);

    ChatRoom getOrCreateRoom(Long userId, Long adminId);

    ChatRoom findAndSetVisible(Long roomId);
}
