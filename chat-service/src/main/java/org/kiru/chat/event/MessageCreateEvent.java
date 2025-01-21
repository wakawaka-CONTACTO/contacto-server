package org.kiru.chat.event;


import lombok.Builder;

@Builder
public record MessageCreateEvent(
        Long messageId, Long userId,String content) {
    public static MessageCreateEvent of(Long messageId,Long userId, String content) {
        return MessageCreateEvent.builder()
                .messageId(messageId)
                .userId(userId)
                .content(content)
                .build();
    }
}
