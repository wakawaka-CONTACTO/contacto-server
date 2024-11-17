package org.kiru.core.chat.chatroom.domain;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.kiru.core.chat.chatroom.entity.ChatRoomJpaEntity;
import org.kiru.core.chat.message.domain.Message;

@Getter
@AllArgsConstructor
@Builder
public class ChatRoom {
    @NotNull
    private Long id;

    @NotNull
    private String title;

    @Nullable
    @Builder.Default
    private final List<Message> messages = new ArrayList<>();

    @NotNull
    private ChatRoomType type;

    @Nullable
    private final List<Long> participants;

    private final String chatRoomThumbnail;

    @Setter
    private int unreadMessageCount;

    @Setter
    private String latestMessageContent;

    public void addMessage(final List<Message> message) {
        Objects.requireNonNull(this.messages).addAll(message);
    }

    public boolean addParticipant(final Long userId) {
        if (this.participants != null && this.type == ChatRoomType.PRIVATE && !this.participants.contains(userId)) {
            return this.participants.add(userId);
        }
        return false;
    }

    public static ChatRoom of(String title, ChatRoomType type) {
        return ChatRoom.builder()
                .title(title)
                .type(type)
                .messages(new ArrayList<>())
                .participants(new ArrayList<>())
                .build();
    }

    public static ChatRoom fromEntity(ChatRoomJpaEntity chatRoomJpa){
        return ChatRoom.builder()
                .id(chatRoomJpa.getId())
                .title(chatRoomJpa.getTitle())
                .type(chatRoomJpa.getType())
                .messages(new ArrayList<>())
                .participants(new ArrayList<>())
                .build();
    }
}