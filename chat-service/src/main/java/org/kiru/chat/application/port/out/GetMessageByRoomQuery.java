package org.kiru.chat.application.port.out;

import java.util.List;
import org.kiru.core.chat.message.domain.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface GetMessageByRoomQuery {
    List<Message> findAllByChatRoomIdWithMessageToRead(Long chatRoomId, Long userId, Boolean isUserAdmin);

    List<Message> getMessages(Long roomId, Long userId, Boolean isUserAdmin, Pageable pageable);
}
