package org.kiru.chat.application.port.in;


public interface AddParticipantUseCase {
    boolean addParticipant(Long roomId, Long userId);
}
