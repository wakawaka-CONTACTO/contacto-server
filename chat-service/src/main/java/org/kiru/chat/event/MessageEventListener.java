package org.kiru.chat.event;

import java.util.Map;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kiru.chat.application.port.out.SaveMessagePort;
import org.kiru.chat.application.port.out.SendMessagePort;
import org.kiru.core.chat.message.domain.Message;
import org.kiru.core.exception.ContactoException;
import org.kiru.core.exception.code.FailureCode;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class MessageEventListener implements StreamListener<String, MapRecord<String,String,String>> {
    private final SimpMessagingTemplate messagingTemplate;
    private final SaveMessagePort saveMessagePort;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final SendMessagePort sendMessagePort;

    @Override
    @Transactional
    public void onMessage(final MapRecord<String, String, String> message) throws ContactoException {
        try {
            String recordId = message.getId().getValue();
            Map<String, String> map = message.getValue();
            Message saveMessage = saveMessagePort.save(Message.of(map));
            messagingTemplate.convertAndSend(
                    "/topic/" + saveMessage.getChatRoomId(),
                    saveMessage
            );
            String receiverId = map.get("receiverId");
            sendMessagePort.sendMessageAck(recordId);
            applicationEventPublisher.publishEvent(MessageCreateEvent.of(receiverId,saveMessage));
        } catch (RedisSystemException e) {
            log.error("Redis Listener Error: ERROR: {}", e.getMessage());
            throw new ContactoException(FailureCode.CHAT_MESSAGE_SEND_FAILED);
        } catch (Exception e) {
            log.error("General Listener Error: ERROR: {}", e.getMessage());
            throw new ContactoException(FailureCode.INTERNAL_SERVER_ERROR);
        }
    }
}