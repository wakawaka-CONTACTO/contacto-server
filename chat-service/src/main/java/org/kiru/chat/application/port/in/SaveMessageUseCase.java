package org.kiru.chat.application.port.in;

import org.kiru.core.chat.message.domain.Message;

public interface SaveMessageUseCase {
    Message saveMessage(Long roomId, Message message);
}
