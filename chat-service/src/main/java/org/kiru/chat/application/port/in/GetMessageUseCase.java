package org.kiru.chat.application.port.in;

import java.util.List;
import org.kiru.core.chat.message.domain.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface GetMessageUseCase {
    List<Message> getMessages(Long roomId, Long userId, Boolean isAdmin,Pageable pageable);
}