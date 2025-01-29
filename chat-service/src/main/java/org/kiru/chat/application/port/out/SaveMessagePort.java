package org.kiru.chat.application.port.out;

import java.util.List;
import org.kiru.core.chat.message.domain.Message;
import org.kiru.core.chat.message.entity.MessageJpaEntity;

public interface SaveMessagePort {
    Message save(Message message);
    List<Message> saveAll(List<MessageJpaEntity> messages);
}
