package org.kiru.chat.adapter.in.web.req;

import java.util.List;
import org.kiru.core.chat.message.domain.TranslateLanguage;

public record OriginMessageDto(
        List<Long> messageIds,
        TranslateLanguage translateLanguage
) {
    public static OriginMessageDto of(List<Long> messageIds, TranslateLanguage translateLanguage) {
        return new OriginMessageDto(messageIds, translateLanguage);
    }

    public static OriginMessageDto of(Long messageId, TranslateLanguage translateLanguage) {
        return new OriginMessageDto(List.of(messageId), translateLanguage);
    }
}
