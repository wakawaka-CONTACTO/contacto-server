package org.kiru.chat.application.port.out;

import org.kiru.core.chat.message.domain.Message;

public interface SaveMessagePort {
    Message save(Message message);
}
