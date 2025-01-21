package org.kiru.chat.adapter.in.web;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.kiru.chat.adapter.in.web.req.OriginMessageDto;
import org.kiru.chat.application.port.in.MessageTranslateUseCase;
import org.kiru.core.chat.message.domain.TranslateMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/chat/translates")
@RestController
@RequiredArgsConstructor
public class TranslateController {
    private final MessageTranslateUseCase translateUseCase;

    @PostMapping
    public ResponseEntity<List<TranslateMessage>> translate(@RequestBody OriginMessageDto message) {
        return ResponseEntity.ok(translateUseCase.translateMessage(message));
    }
}
