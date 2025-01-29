package org.kiru.chat.adapter.out.persistence.dto;

import org.kiru.core.chat.chatroom.entity.ChatRoomJpaEntity;

public interface ChatRoomProjection {
    ChatRoomJpaEntity getChatRoom();
    int getUnreadMessageCount();
    String getLatestMessageContent();
    String getParticipants();
}
