package org.kiru.chat.event;

import static java.util.Objects.requireNonNull;

import lombok.Builder;

@Builder
public record MessageCreateEvent(
        Long messageId, String userId,String content) {
    public static MessageCreateEvent of(Long messageId,String userId, String content) {
        return MessageCreateEvent.builder()
                .messageId(messageId)
                .userId(userId)
                .content(content)
                .build();
    }

    public MessageCreateEvent {
        requireNonNull(messageId, "messageID가 필요합니다.");
        requireNonNull(userId, "userId가 필요합니다.");
        requireNonNull(content, "content가 필요합니다.");
    }
}
