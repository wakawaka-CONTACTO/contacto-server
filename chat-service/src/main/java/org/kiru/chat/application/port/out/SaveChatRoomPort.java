package org.kiru.chat.application.port.out;

import org.kiru.core.chat.chatroom.domain.ChatRoom;

public interface SaveChatRoomPort {
    ChatRoom save(ChatRoom chatRoom,Long userId,Long userId2);
}
