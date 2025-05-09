package org.kiru.chat.adapter.out.persistence;

import jakarta.persistence.QueryHint;
import org.kiru.chat.adapter.in.web.res.AdminUserResponse;
import org.kiru.chat.adapter.out.persistence.dto.ChatRoomProjection;
import org.kiru.core.chat.userchatroom.entity.UserJoinChatRoom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import java.util.List;
import java.util.Optional;


public interface UserJoinChatRoomRepository extends JpaRepository<UserJoinChatRoom, Long> {

    @Query("SELECT u.userId FROM UserJoinChatRoom u WHERE u.chatRoomId = :chatRoomId AND u.userId <> :senderId")
    List<Long> findOtherParticipantIds(Long chatRoomId, Long senderId);

    @QueryHints(value = {
            @QueryHint(name = "org.hibernate.cacheable", value = "false"),
            @QueryHint(name = "jakarta.persistence.query.timeout", value = "5000")
    })
    @Query("SELECT cr as chatRoom, " +
            "COUNT(CASE WHEN m.readStatus = false AND m.senderId <> :userId THEN m.id END) AS unreadMessageCount, " +
            "(SELECT m1.content"
            + "        FROM MessageJpaEntity m1"
            + "        WHERE m1.chatRoomId = cr.id"
            + "        ORDER BY m1.id DESC"
            + "        LIMIT 1) AS latestMessageContent," +
            "STRING_AGG(CAST(uj.userId AS string), ',') AS participants " +
            "FROM ChatRoomJpaEntity cr " +
            "LEFT JOIN UserJoinChatRoom uj ON cr.id = uj.chatRoomId " +
            "LEFT JOIN MessageJpaEntity m ON cr.id = m.chatRoomId " +
            "WHERE uj.chatRoomId IN (SELECT uj2.chatRoomId FROM UserJoinChatRoom uj2 WHERE uj2.userId = :userId)" +
            "GROUP BY cr.id " +
            "ORDER BY (SELECT MAX(m2.createdAt) FROM MessageJpaEntity m2 WHERE m2.chatRoomId = cr.id) DESC NULLS LAST")
    Slice<ChatRoomProjection> findChatRoomsByUserIdWithUnreadMessageCountAndLatestMessageAndParticipants(Long userId, Pageable pageable);

    @Query("SELECT u.userId FROM UserJoinChatRoom u " +
            "JOIN UserJoinChatRoom uj ON u.chatRoomId = uj.chatRoomId " +
            "WHERE uj.userId = :userId AND u.userId <> :userId")
    List<Long> findAlreadyLikedUserIds(Long userId);


    @Query("SELECT new org.kiru.chat.adapter.in.web.res.AdminUserResponse(u.userId, cr.createdAt) FROM UserJoinChatRoom u " +
            "JOIN UserJoinChatRoom uj ON u.chatRoomId = uj.chatRoomId " +
            "INNER JOIN ChatRoomJpaEntity cr ON u.chatRoomId = cr.id " +
            "WHERE uj.userId = :userId AND u.userId <> :userId")
    List<AdminUserResponse> getMatchedUser(Long userId);

    @Query("SELECT u FROM UserJoinChatRoom u "
            + "JOIN UserJoinChatRoom uj ON u.chatRoomId = uj.chatRoomId "
            + "WHERE u.userId = :userId "
            + "AND uj.userId = :adminId")
    List<UserJoinChatRoom> findByUserIdAndAdminId(Long userId, Long adminId);

    @Query("FROM UserJoinChatRoom u "
            + "WHERE u.chatRoomId IN ( "
            + "    SELECT uj.chatRoomId "
            + "    FROM UserJoinChatRoom uj "
            + "    WHERE uj.userId = :userId "
            + ")"
            + "AND u.userId = :userId2")
    Optional<UserJoinChatRoom> findAlreadyRoomByUserIds(Long userId, Long userId2);

    @Query("FROM UserJoinChatRoom u "
        + "WHERE u.chatRoomId IN ( "
        + "    SELECT uj.chatRoomId "
        + "    FROM UserJoinChatRoom uj "
        + "    WHERE uj.userId = :userId "
        + ")"
        + "AND u.userId = :userId2")
    List<UserJoinChatRoom> findAllByUserIds(Long userId, Long userId2);
}
