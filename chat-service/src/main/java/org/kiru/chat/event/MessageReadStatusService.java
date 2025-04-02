package org.kiru.chat.event;

import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.kiru.chat.adapter.out.persistence.MessageRepository;
import org.kiru.core.chat.message.entity.MessageJpaEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MessageReadStatusService {
    private final MessageRepository messageRepository;
    
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * 특정 채팅방의 메시지를 읽음 처리합니다.
     * 이 메서드는 명시적으로 호출될 때만 메시지를 읽음 처리합니다.
     */
    @Transactional
    public void markAllMessagesAsRead(Long chatRoomId, Long userId) {
        // @Modifying 쿼리를 사용하여 직접 업데이트 - 캐시 문제를 방지
        int updatedCount = messageRepository.markMessagesAsRead(chatRoomId, userId);
    }
    
    /**
     * 채팅방의 안 읽은 메시지 수를 반환합니다.
     * 이 메서드는 메시지의 읽음 상태를 변경하지 않고 단순히 조회만 합니다.
     */
    public int getUnreadMessageCount(Long chatRoomId, Long userId) {
        return messageRepository.countUnreadMessagesByChatRoomIdAndUserId(chatRoomId, userId);
    }
}
