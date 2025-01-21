package org.kiru.chat.event;

import static java.util.Objects.requireNonNull;

import lombok.Builder;

@Builder
public record MessageCreateEvent(
        Long messageId, String userId,String content) {
    /**
     * Creates a new MessageCreateEvent instance using the builder pattern.
     *
     * @param messageId The unique identifier of the message, must not be null
     * @param userId The identifier of the user who created the message, must not be null
     * @param content The content of the message, must not be null
     * @return A new MessageCreateEvent instance with the specified details
     * @throws IllegalArgumentException if any of the parameters are null
     */
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
