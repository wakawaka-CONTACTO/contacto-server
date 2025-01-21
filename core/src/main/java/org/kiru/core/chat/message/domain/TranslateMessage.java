package org.kiru.core.chat.message.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.kiru.core.chat.message.entity.TranslateMessageJpaEntity;

@AllArgsConstructor
@Builder
@Getter
public class TranslateMessage {
    private final Long id;
    private final String message;

    public static TranslateMessage of(final TranslateMessageJpaEntity entity) {
        return TranslateMessage.builder()
                .id(entity.getMessageId())
                .message(entity.getMessage())
                .build();
    }
}
