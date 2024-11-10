package org.kiru.core.chat.message.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.kiru.core.chat.common.BaseTimeEntity;
import org.kiru.core.chat.message.domain.Message;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "messages")
@Getter
public class MessageJpaEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content", length = 512)
    private String content;

    @Column(name = "sender_id")
    private Long senderId;

    @JoinColumn(name = "chat_room_id")
    private Long chatRoomId;

    public static MessageJpaEntity of(Message message) {
        return MessageJpaEntity.builder()
                .chatRoomId(message.getChatRoomId())
                .content(message.getContent())
                .senderId(message.getSenderId())
                .content(message.getContent())
                .build();
    }

    public static Message fromEntity(MessageJpaEntity messageJpaEntity) {
        return Message.builder()
                .chatRoomId(messageJpaEntity.getChatRoomId())
                .content(messageJpaEntity.getContent())
                .senderId(messageJpaEntity.getSenderId())
                .content(messageJpaEntity.getContent())
                .createdAt(messageJpaEntity.getCreatedAt())
                .build();
    }
}