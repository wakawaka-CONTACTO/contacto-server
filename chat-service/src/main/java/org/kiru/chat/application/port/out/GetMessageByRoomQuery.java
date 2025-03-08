package org.kiru.chat.application.port.out;

import java.util.List;
import org.kiru.core.chat.message.domain.Message;
import org.kiru.core.common.PageableResponse;
import org.springframework.data.domain.Pageable;

public interface GetMessageByRoomQuery {
    List<Message> findAllByChatRoomIdWithMessageToRead(Long chatRoomId, Long userId, Boolean isUserAdmin);

    PageableResponse<Message> getMessages(Long roomId, Long userId, Boolean isUserAdmin, Pageable pageable);
}
