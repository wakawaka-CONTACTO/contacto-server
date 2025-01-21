package org.kiru.chat.adapter.in.web.req;

import static java.util.Objects.requireNonNull;

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

    public OriginMessageDto{
        requireNonNull(messageIds, "messageIds가 필요합니다.");
        requireNonNull(translateLanguage, "번역 언어가 필요합니다.");
    }
}
