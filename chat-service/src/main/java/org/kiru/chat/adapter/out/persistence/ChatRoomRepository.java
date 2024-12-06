package org.kiru.chat.adapter.out.persistence;


import org.kiru.core.chat.chatroom.entity.ChatRoomJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoomJpaEntity, Long> {

}