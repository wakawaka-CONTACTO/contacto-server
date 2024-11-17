package org.kiru.core.chat.message.domain;
import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    private Long id;
    @Nonnull
    private String content;
    @Nonnull
    private Long senderId;

    private Long sendedId;
    @Nonnull
    private Long chatRoomId;

    private LocalDateTime createdAt;
    @Setter
    private boolean readStatus; // 읽음 상태 추가

    public void chatRoom(Long chatRoomId){
        this.chatRoomId = chatRoomId;
    }
    public static Message of(Long id, String content, Long senderId, LocalDateTime createdAt, Long chatRoomId,Long sendedId, boolean readStatus) {
        return Message.builder()
                .id(id)
                .content(content)
                .senderId(senderId)
                .createdAt(createdAt)
                .chatRoomId(chatRoomId)
                .readStatus(readStatus) // 읽음 상태 추가
                .sendedId(sendedId)
                .build();
    }
}

