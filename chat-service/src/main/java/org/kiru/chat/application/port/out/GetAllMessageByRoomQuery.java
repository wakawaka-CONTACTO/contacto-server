package org.kiru.chat.application.port.out;

import java.util.List;
import org.kiru.core.chat.message.domain.Message;

public interface GetAllMessageByRoomQuery {
    List<Message> findAllByChatRoomId(Long chatRoomId);
}
