package org.kiru.chat.adapter.out.persistence;

import java.util.List;
import org.kiru.chat.adapter.in.web.res.AdminUserResponse;
import org.kiru.core.chat.userchatroom.entity.UserJoinChatRoom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;


public interface UserJoinChatRoomRepository extends JpaRepository<UserJoinChatRoom, Long> {

    @Query("SELECT u.userId FROM UserJoinChatRoom u WHERE u.chatRoomId = :chatRoomId AND u.userId <> :senderId")
    List<Long> findOtherParticipantIds(Long chatRoomId, Long senderId);

    @Query("SELECT cr, " +
            "COUNT(m.id)/2 AS unreadMessageCount, " +
            "MAX(m.content) AS latestMessageContent, " +
            "STRING_AGG(CAST(uj.userId AS string), ',') AS participants " +
            "FROM ChatRoomJpaEntity cr " +
            "LEFT JOIN UserJoinChatRoom uj ON cr.id = uj.chatRoomId " +
            "LEFT JOIN MessageJpaEntity m ON cr.id = m.chatRoomId AND m.readStatus = false AND m.senderId <> :userId " +
            "WHERE uj.chatRoomId IN (SELECT uj2.chatRoomId FROM UserJoinChatRoom uj2 WHERE uj2.userId = :userId) " +
            "GROUP BY cr.id")
    Slice<Object[]> findChatRoomsByUserIdWithUnreadMessageCountAndLatestMessageAndParticipants(Long userId,
                                                                                               Pageable pageable);

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
    @Transactional
    List<UserJoinChatRoom> findByUserIdAndAdminId(Long userId, Long adminId);
}
