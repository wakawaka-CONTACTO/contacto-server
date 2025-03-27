package org.kiru.chat.application.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kiru.chat.adapter.in.web.req.OriginMessageDto;
import org.kiru.chat.adapter.out.persistence.MessageRepository;
import org.kiru.chat.adapter.out.persistence.TranslateMessageRepository;
import org.kiru.chat.adapter.out.persistence.dto.MessageWithTranslationDto;
import org.kiru.chat.application.api.TranslateApi;
import org.kiru.chat.application.api.req.TranslationRequest;
import org.kiru.chat.application.api.res.TranslationResponse;
import org.kiru.chat.application.port.in.MessageTranslateUseCase;
import org.kiru.core.chat.message.domain.TranslateLanguage;
import org.kiru.core.chat.message.domain.TranslateMessage;
import org.kiru.core.chat.message.entity.MessageJpaEntity;
import org.kiru.core.chat.message.entity.TranslateMessageJpaEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranslateService implements MessageTranslateUseCase {
    private final TranslateApi translateApi;
    private final MessageRepository messageRepository;
    private final TranslateMessageRepository translateMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void translateMessages(String userId, List<Long> messageIds) {
        try {
            List<TranslateMessage> translatedMessages = translateMessage(
                OriginMessageDto.of(messageIds, TranslateLanguage.KO)
            );
            
            messagingTemplate.convertAndSend(
                "/queue/translate/" + userId,
                translatedMessages
            );
            log.info("Translation sent to user {} for messages {}", userId, messageIds);
        } catch (Exception e) {
            log.error("Translation failed for user {} and messages {}: {}", 
                userId, messageIds, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public List<TranslateMessage> translateMessage(OriginMessageDto messageDto) {
        Map<Long, String> notTranslateMessage = new LinkedHashMap<>();
        List<TranslateMessageJpaEntity> alreadyTranslateMessageJpaEntity = new ArrayList<>();

        List<Long> messageIds = messageDto.messageIds();
        List<MessageWithTranslationDto> messages = messageRepository.findAllByMessageIds(messageIds);
        
        // 2. 번역되지 않은 메시지 수집
        for (MessageWithTranslationDto message : messages) {
            MessageJpaEntity messageJpaEntity = message.getMessage();
            TranslateMessageJpaEntity translateMessageJpaEntity = message.getTranslateMessage();
            if (translateMessageJpaEntity == null) {
                notTranslateMessage.put(messageJpaEntity.getId(), messageJpaEntity.getContent());
            } else {
                alreadyTranslateMessageJpaEntity.add(translateMessageJpaEntity);
            }
        }

        if (notTranslateMessage.isEmpty()) {
            return alreadyTranslateMessageJpaEntity.stream()
                .map(TranslateMessage::of)
                .toList();
        }

        // 3. 새로운 메시지 번역
        List<String> textsToTranslate = new ArrayList<>(notTranslateMessage.values());
        TranslationRequest request = TranslationRequest.builder()
            .texts(textsToTranslate)
            .tl(messageDto.translateLanguage().toString().toLowerCase())
            .sl("auto")
            .build();

        TranslationResponse response = translateApi.translateHtml(request);
        List<String> translatedTexts = response.texts();
        List<Long> keyIds = List.of(notTranslateMessage.keySet().toArray(new Long[0]));

        // 4. 번역된 메시지 저장
        for (int i = 0; i < textsToTranslate.size(); i++) {
            log.debug("Translated text for message ID {} processed.", keyIds.get(i));
            TranslateMessageJpaEntity translateMessageJpaEntity = TranslateMessageJpaEntity.builder()
                .message(translatedTexts.get(i))
                .messageId(keyIds.get(i))
                .translateLanguage(messageDto.translateLanguage())
                .build();
            alreadyTranslateMessageJpaEntity.add(translateMessageJpaEntity);
        }

        return translateMessageRepository.saveAll(alreadyTranslateMessageJpaEntity)
            .stream()
            .map(TranslateMessage::of)
            .toList();
    }
}
