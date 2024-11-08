package org.kiru.user.user.service;


import java.util.List;
import lombok.RequiredArgsConstructor;
import org.kiru.core.talent.entity.UserTalent;
import org.kiru.user.user.dto.event.UserCreateEvent;
import org.kiru.user.user.repository.UserTalentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class UserTalentService {
    private final UserTalentRepository userTalentRepository;

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void userTalentsCreate(UserCreateEvent userCreateEvent){
        Long userId = userCreateEvent.userId();
        List<UserTalent> userTalents = userCreateEvent.talents().stream()
                .map(i -> UserTalent.builder().userId(userId).talentType(i.talentType()).build()).toList();
        userTalentRepository.saveAll(userTalents);
    }
}
