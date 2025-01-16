package org.kiru.core.chat.userchatroom.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Table(
        name="user_chat",
        uniqueConstraints={
                @UniqueConstraint(
                        columnNames={"user_id", "chat_room_id"}
                )
        },
        indexes = {
                @Index(name = "user_chat_chat_room_id_idx", columnList = "chat_room_id"),
                @Index(name = "user_chat_user_id_idx", columnList = "user_id")
        }
)
@Getter
@Entity
public class UserJoinChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "chat_room_id")
    private Long chatRoomId;
}
