package org.kiru.chat.application.service;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kiru.chat.application.port.in.SaveMessageUseCase;
import org.kiru.chat.application.port.in.SendMessageUseCase;
import org.kiru.chat.application.port.out.GetChatRoomQuery;
import org.kiru.chat.application.port.out.SaveMessagePort;
import org.kiru.chat.application.port.out.SendMessagePort;
import org.kiru.core.chat.chatroom.domain.ChatRoom;
import org.kiru.core.chat.message.domain.Message;
import org.kiru.core.exception.EntityNotFoundException;
import org.kiru.core.exception.ForbiddenException;
import org.kiru.core.exception.code.FailureCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService implements SendMessageUseCase, SaveMessageUseCase {
    private final GetChatRoomQuery getChatRoomQuery;
    private final SaveMessagePort saveMessagePort;
    private final SendMessagePort sendMessagePort;

    @Transactional
    public Message sendMessage(final Long roomId, Message message) {
        ChatRoom chatRoom = getChatRoomQuery.findAndSetVisible(roomId);
        if (chatRoom == null) {
            throw new EntityNotFoundException(FailureCode.CHATROOM_NOT_FOUND);
        }
        try {
            Objects.requireNonNull(chatRoom.getMessages()).add(message);
            Message saveMessage = saveMessagePort.save(message);
            sendMessagePort.sendMessage(saveMessage, message.getSendedId().toString());
            return saveMessage;
        } catch (Exception e) {
            log.error("Failed to send message", e);
            throw new ForbiddenException(FailureCode.CHAT_MESSAGE_SEND_FAILED);
        }
    }

    @Override
    public Message saveMessage(Long roomId, Message message) {
        message.chatRoom(roomId);
        return saveMessagePort.save(message);
    }
}