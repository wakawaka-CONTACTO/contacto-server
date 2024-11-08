package org.kiru.chat.application.port.in;

import org.kiru.core.message.domain.Message;

public interface SendMessageUseCase {
    Message sendMessage(Long roomId, Message message);
}
