package org.kiru.chat.application.port.in;

import java.util.List;
import org.kiru.chat.adapter.in.web.req.OriginMessageDto;
import org.kiru.core.chat.message.domain.TranslateMessage;

public interface MessageTranslateUseCase {
    List<TranslateMessage> translateMessage(OriginMessageDto originMessageDto);
}
