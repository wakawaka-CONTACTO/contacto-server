package org.kiru.chat.application.port.in;

import java.util.List;
import org.kiru.core.chat.chatroom.domain.ChatRoom;

public interface GetChatRoomUseCase {
    ChatRoom findRoomById(Long roomId, Long userId);
    List<ChatRoom> findRoomsByUserId(Long userId);
}
