package org.kiru.core.chat.message.domain;

import lombok.Builder;
import org.kiru.core.chat.message.entity.TranslateMessageJpaEntity;
import org.kiru.core.exception.InvalidValueException;
import org.kiru.core.exception.code.FailureCode;

@Builder
public record TranslateMessage(Long id, String message) {
    public static TranslateMessage of(final TranslateMessageJpaEntity entity) {
        if (entity == null) {
            throw new InvalidValueException(FailureCode.ENTITY_NOT_FOUND);
        }
        return TranslateMessage.builder()
                .id(entity.getMessageId())
                .message(entity.getMessage())
                .build();
    }
}
