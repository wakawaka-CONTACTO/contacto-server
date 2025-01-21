package org.kiru.chat.event;

import java.util.List;
import org.kiru.core.chat.message.domain.TranslateLanguage;

public record UserTranslateSubscribeEvent(
    String userId,
    TranslateLanguage translateLanguage,
    List<Long> messageIds
) {}