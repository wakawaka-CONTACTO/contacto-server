package org.kiru.core.chat.chatroom.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.kiru.core.chat.chatroom.entity.ChatRoomJpaEntity;
import org.kiru.core.chat.message.domain.Message;
import java.util.HashSet;

@Getter
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatRoom {
    @NotNull
    private Long id;

    @NotNull
    @Setter
    private String title;

    @Nullable
    @Builder.Default
    private final List<Message> messages = new ArrayList<>();

    @NotNull
    private ChatRoomType type;

    @Nullable
    private final Set<Long> participants; // Set으로 변경

    @Setter
    private String chatRoomThumbnail;

    @Setter
    private int unreadMessageCount;

    @Setter
    private String latestMessageContent;

    public void addMessage(final List<Message> message) {
        Objects.requireNonNull(this.messages).addAll(message);
    }

    public void addMessage(final Message message) {
        Objects.requireNonNull(this.messages).add(message);
    }

    public boolean addParticipant(final Long userId) {
        if (this.participants != null && this.type == ChatRoomType.PRIVATE) {
            return this.participants.add(userId);
        }
        return false;
    }

    public boolean removeParticipant(final Long userId) {
        if (this.participants != null && this.type == ChatRoomType.PRIVATE) {
            return this.participants.remove(userId);
        }
        return false;
    }

    public void addParticipants(final List<Long> userIds) {
        if (this.participants != null && this.type == ChatRoomType.PRIVATE) {
            this.participants.addAll(userIds);
        }
    }

    public static ChatRoom of(String title, ChatRoomType type) {
        return ChatRoom.builder()
                .title(title)
                .type(type)
                .messages(new ArrayList<>())
                .participants(new HashSet<>())
                .build();
    }

    public static ChatRoom fromEntity(ChatRoomJpaEntity chatRoomJpa){
        return ChatRoom.builder()
                .id(chatRoomJpa.getId())
                .title(chatRoomJpa.getTitle())
                .type(chatRoomJpa.getType())
                .messages(new ArrayList<>())
                .participants(new HashSet<>())
                .build();
    }
}