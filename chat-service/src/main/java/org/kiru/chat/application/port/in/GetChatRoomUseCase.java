package org.kiru.chat.application.port.in;

import org.kiru.core.common.PageableResponse;
import org.kiru.core.chat.chatroom.domain.ChatRoom;
import org.springframework.data.domain.Pageable;

public interface GetChatRoomUseCase {
    ChatRoom findRoomById(Long roomId, Long userId, boolean isUserAdmin);
    PageableResponse<ChatRoom> findRoomsByUserId(Long userId, Pageable pageable);
    ChatRoom getOrCreateRoomUseCase(Long userId, Long adminId);
}
