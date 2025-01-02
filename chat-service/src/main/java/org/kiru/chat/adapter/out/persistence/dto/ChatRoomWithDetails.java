package org.kiru.chat.adapter.out.persistence.dto;

import org.kiru.core.chat.chatroom.entity.ChatRoomJpaEntity;
import org.kiru.core.chat.message.entity.MessageJpaEntity;


public record ChatRoomWithDetails(
        ChatRoomJpaEntity chatRoom,
        MessageJpaEntity message,
        Long userId
) {}