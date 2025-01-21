package org.kiru.chat.adapter.out.persistence.dto;

import org.kiru.core.chat.message.entity.MessageJpaEntity;
import org.kiru.core.chat.message.entity.TranslateMessageJpaEntity;

public interface MessageWithTranslationDto {
    /**
 * Retrieves the original message entity associated with this data transfer object.
 *
 * @return the {@link MessageJpaEntity} representing the original message
 */
MessageJpaEntity getMessage();
    /**
 * Retrieves the translated message entity associated with this message.
 *
 * @return the translated message entity, or null if no translation exists
 */
TranslateMessageJpaEntity getTranslateMessage();
}