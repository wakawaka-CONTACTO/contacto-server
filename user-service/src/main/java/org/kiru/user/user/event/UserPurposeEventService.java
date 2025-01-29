package org.kiru.user.user.event;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.kiru.core.user.userPurpose.entity.UserPurpose;
import org.kiru.user.user.dto.event.UserCreateEvent;
import org.kiru.user.user.repository.UserPurposeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class UserPurposeEventService {
    private final UserPurposeRepository userPurposeRepository;

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void userPurposeCreate(UserCreateEvent userCreateEvent){
        Long userId = userCreateEvent.userId();
        List<UserPurpose> userPurposes = userCreateEvent.purposes().stream()
                .map(i -> UserPurpose.builder().userId(userId).purposeType(i.purposeType()).build()).toList();
        userPurposeRepository.saveAll(userPurposes);
    }
}
