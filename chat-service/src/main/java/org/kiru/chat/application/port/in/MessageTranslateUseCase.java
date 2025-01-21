package org.kiru.chat.application.port.in;

import java.util.List;
import org.kiru.chat.adapter.in.web.req.OriginMessageDto;
import org.kiru.core.chat.message.domain.TranslateMessage;

public interface MessageTranslateUseCase {
    /**
 * Translates an origin message to one or more translated messages.
 *
 * @param originMessageDto The original message to be translated, containing source language and content
 * @return A list of translated messages, potentially in multiple target languages
 * @throws IllegalArgumentException If the input message is null or invalid
 */
List<TranslateMessage> translateMessage(OriginMessageDto originMessageDto);
}
