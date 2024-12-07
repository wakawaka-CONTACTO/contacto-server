package org.kiru.chat.application.port.in;

import java.util.List;
import org.kiru.core.chat.chatroom.domain.ChatRoom;

public interface GetChatRoomUseCase {
    ChatRoom findRoomById(Long roomId, Long userId, Boolean isUserAdmin);
    List<ChatRoom> findRoomsByUserId(Long userId);
    ChatRoom getOrCreateRoomUseCase(Long userId, Long adminId);
}
