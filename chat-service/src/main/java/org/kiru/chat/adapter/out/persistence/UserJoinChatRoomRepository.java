package org.kiru.chat.adapter.out.persistence;

import java.util.List;
import org.kiru.chat.adapter.in.web.res.AdminUserResponse;
import org.kiru.core.chat.userchatroom.entity.UserJoinChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserJoinChatRoomRepository extends JpaRepository<UserJoinChatRoom, Long> {
    @Query("SELECT cr, " +
            "COUNT(m) AS unreadMessageCount, " +
            "lm.content AS latestMessageContent " +
            "FROM ChatRoomJpaEntity cr " +
            "JOIN UserJoinChatRoom uj ON cr.id = uj.chatRoomId " +
            "LEFT JOIN MessageJpaEntity m ON cr.id = m.chatRoomId AND m.readStatus = false AND m.senderId <> :userId " +
            "LEFT JOIN MessageJpaEntity lm ON cr.id = lm.chatRoomId AND lm.createdAt = (" +
            "SELECT MAX(lm2.createdAt) FROM MessageJpaEntity lm2 WHERE lm2.chatRoomId = cr.id) " +
            "WHERE uj.userId = :userId " +
            "GROUP BY cr.id, lm.content")
    List<Object[]> findChatRoomsByUserIdWithUnreadMessageCountAndLatestMessage(@Param("userId") Long userId);
    @Query("SELECT u.userId FROM UserJoinChatRoom u WHERE u.chatRoomId = :chatRoomId AND u.userId <> :senderId")
    List<Long> findOtherParticipantIds(Long chatRoomId, Long senderId);

    @Query("SELECT u.userId FROM UserJoinChatRoom u WHERE u.chatRoomId = :chatRoomId")
    List<Long> findParticipantIdsByChatRoomId(@Param("chatRoomId") Long chatRoomId);

    @Query("SELECT cr, " +
            "COUNT(DISTINCT m.id) AS unreadMessageCount, " +
            "lm.content AS latestMessageContent, " +
            "STRING_AGG(CAST(uj.userId AS string), ',') AS participants " +
            "FROM ChatRoomJpaEntity cr " +
            "JOIN UserJoinChatRoom uj ON cr.id = uj.chatRoomId " +
            "LEFT JOIN MessageJpaEntity m ON cr.id = m.chatRoomId AND m.readStatus = false AND m.senderId <> :userId " +
            "LEFT JOIN MessageJpaEntity lm ON cr.id = lm.chatRoomId AND lm.createdAt = (" +
            "SELECT MAX(lm2.createdAt) FROM MessageJpaEntity lm2 WHERE lm2.chatRoomId = cr.id) " +
            "WHERE uj.chatRoomId IN (SELECT uj2.chatRoomId FROM UserJoinChatRoom uj2 WHERE uj2.userId = :userId) " +
            "GROUP BY cr.id, lm.content")
    List<Object[]> findChatRoomsByUserIdWithUnreadMessageCountAndLatestMessageAndParticipants(Long userId);

    @Query("SELECT u.userId FROM UserJoinChatRoom u " +
            "JOIN UserJoinChatRoom uj ON u.chatRoomId = uj.chatRoomId " +
            "WHERE uj.userId = :userId AND u.userId <> :userId")
    List<Long> findAlreadyLikedUserIds(Long userId);


    @Query("SELECT u.userId , u.createAt FROM UserJoinChatRoom u " +
            "JOIN UserJoinChatRoom uj ON u.chatRoomId = uj.chatRoomId " +
            "WHERE uj.userId = :userId AND u.userId <> :userId")
    List<AdminUserResponse> getMatchedUser(Long userId);
}
