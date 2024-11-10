package org.kiru.chat.application.port.in;

import org.kiru.core.chat.message.domain.Message;

public interface SendMessageUseCase {
    Message sendMessage(Long roomId, Message message);
}
