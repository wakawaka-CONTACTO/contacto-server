package org.kiru.user.user.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kiru.user.external.s3.ImageService;
import org.kiru.user.user.dto.event.UserCreateEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserPortfolioEventService {
    private final ImageService imageService;

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void userPortfolioCreate(UserCreateEvent userCreateEvent){
        log.info("[START] event, UserPortfolioEventService.class ");
        long startTime = System.currentTimeMillis();
        Long userId = userCreateEvent.userId();
        imageService.saveImages(userCreateEvent.images(), userId, userCreateEvent.userName());
        long endTime = System.currentTimeMillis();
        log.info("[FINISH] event, UserPortfolioEventService.class " + (endTime - startTime));
    }
}
