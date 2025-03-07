package org.kiru.chat.application.port.in;

import java.util.List;
import org.kiru.core.chat.message.domain.Message;
import org.kiru.core.common.PageableResponse;
import org.springframework.data.domain.Pageable;

public interface GetMessageUseCase {
    PageableResponse<Message> getMessages(Long roomId, Long userId, Boolean isAdmin, Pageable pageable);
}