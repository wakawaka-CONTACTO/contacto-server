package org.kiru.chat.adapter.out.persistence.dto;

import org.kiru.core.chat.message.entity.MessageJpaEntity;
import org.kiru.core.chat.message.entity.TranslateMessageJpaEntity;

public interface MessageWithTranslationDto {
    MessageJpaEntity getMessage();
    TranslateMessageJpaEntity getTranslateMessage();
}