package org.kiru.chat.application.port.in;

import java.util.List;
import org.kiru.core.chat.chatroom.domain.ChatRoom;
import org.kiru.core.chat.message.domain.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface GetChatRoomUseCase {
    ChatRoom findRoomById(Long roomId, Long userId, Boolean isUserAdmin);
    List<ChatRoom> findRoomsByUserId(Long userId);

    ChatRoom getOrCreateRoomUseCase(Long userId, Long adminId);
}
