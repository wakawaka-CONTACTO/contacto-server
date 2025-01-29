package org.kiru.chat.event;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import org.kiru.core.chat.message.domain.Message;

@Builder
public record MessageDto(
        String userId,
        Message message
){
    public static MessageDto of(Long userId, Message message) {
        return new MessageDto(userId.toString(), message);
    }

    public MessageDto{
        requireNonNull(userId, "userId가 필요합니다.");
        requireNonNull(message, "message가 필요합니다.");
    }

    public Map<String, String> toMap() {
        Map<String, String> fieldMap = new HashMap<>();
        fieldMap.put("userId", userId);
        fieldMap.put("content", message.getContent());
        fieldMap.put("senderId", message.getSenderId().toString());
        fieldMap.put("receiverId", message.getSendedId().toString());
        fieldMap.put("chatRoomId", message.getChatRoomId().toString());
        fieldMap.put("createdAt", message.getCreatedAt().toString());
        return fieldMap;
    }
}
