package org.kiru.chat.adapter.out.persistence;

public interface GetOtherParticipantQuery {
    Long getOtherParticipantId(Long roomId, Long senderId);
}
