package org.kiru.chat.application.api.res;

import java.util.List;

public record TranslationResponse(
        int code,
        List<String> texts,
        String tl
) {
}