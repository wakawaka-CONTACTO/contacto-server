package org.kiru.core.chat.chatroom.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.kiru.core.chat.chatroom.domain.ChatRoom;
import org.kiru.core.chat.chatroom.domain.ChatRoomType;
import org.kiru.core.chat.common.BaseTimeEntity;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Table(name = "chat_rooms")
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
public class ChatRoomJpaEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String title;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ChatRoomType type = ChatRoomType.PRIVATE;

    public static ChatRoomJpaEntity of(ChatRoom chatRoom) {
        return ChatRoomJpaEntity.builder()
                .title(chatRoom.getTitle())
                .type(chatRoom.getType())
                .build();
    }
}
