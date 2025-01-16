package org.kiru.chat.application.port.in;

import org.kiru.chat.adapter.in.web.req.CreateChatRoomRequest;
import org.kiru.core.chat.chatroom.domain.ChatRoom;

public interface CreateRoomUseCase {
    ChatRoom createRoom(CreateChatRoomRequest createChatRoomRequest);
}
