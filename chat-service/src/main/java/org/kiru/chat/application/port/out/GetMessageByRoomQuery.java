package org.kiru.chat.application.port.out;

import java.util.List;
import org.kiru.core.chat.message.domain.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface GetMessageByRoomQuery {
    List<Message> findAllByChatRoomId(Long chatRoomId,Long userId, Boolean isUserAdmin);
    Slice<Message> getMessages(Long roomId, Long userId, Boolean isUserAdmin, Pageable pageable);
}
