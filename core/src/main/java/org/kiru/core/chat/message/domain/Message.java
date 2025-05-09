package org.kiru.core.chat.message.domain;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@Builder
@AllArgsConstructor
public class Message {
    private Long id;

    @NotNull
    private String content;

    @NotNull
    private Long senderId;

    @NotNull
    private Long sendedId;

    private Long chatRoomId;

    private LocalDateTime createdAt;

    @Builder.Default
    private Boolean readStatus = false; // 읽음 상태 추가

    public void chatRoom(Long chatRoomId){
        this.chatRoomId = chatRoomId;
    }

    public void setReadStatus(Boolean readStatus) {
        this.readStatus = readStatus;
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

    public static Message of(Map<String,String> map) {
        return Message.builder()
                .id(map.get("id") == null ? null : Long.parseLong(map.get("id")))
                .content(map.get("content"))
                .senderId(map.get("senderId") == null ? null : Long.parseLong(map.get("senderId")))
                .createdAt(map.get("createdAt") == null ? null : LocalDateTime.parse(map.get("createdAt")))
                .chatRoomId(map.get("chatRoomId") == null ? null : Long.parseLong(map.get("chatRoomId")))
                .readStatus(map.get("readStatus") != null) // 읽음 상태 추가
                .sendedId(map.get("receiverId") == null ? null : Long.parseLong(map.get("receiverId")))
                .build();
    }

    public Map<String,String> toMap(String receiverId) {
        Map<String,String> map = new HashMap<>();
        map.put("id", id != null ? id.toString() : null);
        map.put("content", content);
        map.put("senderId", senderId != null ? senderId.toString() : null);
        map.put("receiverId", receiverId);
        map.put("chatRoomId", chatRoomId != null ? chatRoomId.toString() : null);
        map.put("createdAt", createdAt != null ? createdAt.toString() : null);
        map.put("readStatus", readStatus != null ? readStatus.toString() : "false");
        return map;
    }

    public void toRead(){
        this.readStatus = true;
    }
}

