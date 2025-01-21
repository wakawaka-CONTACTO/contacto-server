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
import org.kiru.chat.application.api.TranslateApi;
import org.kiru.chat.application.api.req.TranslationRequest;
import org.kiru.chat.application.api.res.TranslationResponse;
import org.kiru.chat.application.port.in.MessageTranslateUseCase;
import org.kiru.core.chat.message.domain.TranslateMessage;
import org.kiru.core.chat.message.entity.TranslateMessageJpaEntity;
import org.kiru.core.chat.message.entity.MessageJpaEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranslateService implements MessageTranslateUseCase {
    private final TranslateApi translateApi;
    private final MessageRepository messageRepository;
    private final TranslateMessageRepository translateMessageRepository;

    @Override
    @Transactional
    public List<TranslateMessage> translateMessage(OriginMessageDto messageDto) {
        Map<Long, String> notTranslateMessage = new LinkedHashMap<>();
        List<TranslateMessageJpaEntity> alreadyTranslateMessageJpaEntity = new ArrayList<>();

        List<Long> messageIds = messageDto.messageIds();
        List<Object[]> messages = messageRepository.findAllByMessageIds(messageIds);

        // 2. 번역되지 않은 메시지 수집
        for (Object[] message : messages) {
            MessageJpaEntity messageJpaEntity = (MessageJpaEntity) message[0];
            TranslateMessageJpaEntity translateMessageJpaEntity = (TranslateMessageJpaEntity) message[1];
            if (translateMessageJpaEntity == null) {
                notTranslateMessage.put(messageJpaEntity.getId(), messageJpaEntity.getContent());
            }
            else {
                alreadyTranslateMessageJpaEntity.add(translateMessageJpaEntity);
            }
        }
        if(notTranslateMessage.isEmpty()){
            return alreadyTranslateMessageJpaEntity.stream().map(TranslateMessage::of).toList();
        }
        List<String> textsToTranslate = new ArrayList<>(notTranslateMessage.values());
        TranslationRequest request = TranslationRequest.builder()
                .texts(textsToTranslate)
                .tl(messageDto.translateLanguage().toString().toLowerCase())
                .sl("auto")
                .build();
        TranslationResponse response = translateApi.translateHtml(request);
        List<String> translatedTexts = response.texts();
        List<Long> keyIds = List.of(notTranslateMessage.keySet().toArray(new Long[0]));
        for(int i=0;i<textsToTranslate.size();i++) {
            log.info(translatedTexts.get(i));
            TranslateMessageJpaEntity translateMessageJpaEntity = TranslateMessageJpaEntity.builder()
                    .message(translatedTexts.get(i))
                    .messageId(keyIds.get(i))
                    .translateLanguage(messageDto.translateLanguage())
                    .build();
            alreadyTranslateMessageJpaEntity.add(translateMessageJpaEntity);
        }
        return translateMessageRepository.saveAll(alreadyTranslateMessageJpaEntity).stream().map(TranslateMessage::of).toList();
    }
}
