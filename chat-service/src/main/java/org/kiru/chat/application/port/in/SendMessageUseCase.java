package org.kiru.chat.application.port.in;

import org.kiru.core.chat.message.domain.Message;
import org.kiru.core.chat.message.domain.TranslateLanguage;

public interface SendMessageUseCase {
    Message sendMessage(Long roomId, Message message, boolean isUserConnected, TranslateLanguage translateLanguage);
}
