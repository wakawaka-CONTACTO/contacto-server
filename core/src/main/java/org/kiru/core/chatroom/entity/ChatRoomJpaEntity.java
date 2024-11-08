package org.kiru.core.chatroom.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.kiru.core.chatroom.domain.ChatRoom;
import org.kiru.core.chatroom.domain.ChatRoomType;
import org.kiru.core.common.BaseTimeEntity;

@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Table(name = "chat_rooms")
@Getter
@Entity
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
