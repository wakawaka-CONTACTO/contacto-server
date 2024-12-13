package org.kiru.chat.adapter.out.persistence;

import org.kiru.core.chat.chatroom.entity.ChatRoomJpaEntity;
import org.kiru.core.chat.message.domain.Message;

public class ChatRoomWithDetailsDTO {
    private ChatRoomJpaEntity chatRoom;
    private Message message;
    private Long userId;

    // Constructor
    public ChatRoomWithDetailsDTO(ChatRoomJpaEntity chatRoom, Message message, Long userId) {
        this.chatRoom = chatRoom;
        this.message = message;
        this.userId = userId;
    }
}