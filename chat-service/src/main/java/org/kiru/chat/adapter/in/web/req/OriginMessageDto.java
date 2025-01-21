package org.kiru.chat.adapter.in.web.req;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.kiru.core.chat.message.domain.TranslateLanguage;

public record OriginMessageDto(
        List<Long> messageIds,
        TranslateLanguage translateLanguage
) {
    /**
     * Creates an {@code OriginMessageDto} instance with the specified message IDs and translation language.
     *
     * @param messageIds A list of message identifiers to be translated
     * @param translateLanguage The target language for translation
     * @return A new {@code OriginMessageDto} with the provided message IDs and translation language
     * @throws NullPointerException if messageIds or translateLanguage is null
     */
    public static OriginMessageDto of(List<Long> messageIds, TranslateLanguage translateLanguage) {
        return new OriginMessageDto(messageIds, translateLanguage);
    }

    /**
     * Creates an {@code OriginMessageDto} with a single message ID and specified translation language.
     *
     * @param messageId The unique identifier of the message to be translated
     * @param translateLanguage The target language for translation
     * @return A new {@code OriginMessageDto} instance with the single message ID
     * @throws NullPointerException if messageId or translateLanguage is null
     */
    public static OriginMessageDto of(Long messageId, TranslateLanguage translateLanguage) {
        return new OriginMessageDto(List.of(messageId), translateLanguage);
    }

    public OriginMessageDto{
        requireNonNull(messageIds, "messageIds가 필요합니다.");
        requireNonNull(translateLanguage, "번역 언어가 필요합니다.");
    }
}
