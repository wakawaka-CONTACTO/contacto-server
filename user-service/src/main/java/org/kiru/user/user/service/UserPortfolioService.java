package org.kiru.user.user.service;

import lombok.RequiredArgsConstructor;
import org.kiru.user.external.s3.ImageService;
import org.kiru.user.user.dto.event.UserCreateEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class UserPortfolioService {

    private final ImageService imageService;

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void userPortfolioCreate(UserCreateEvent userCreateEvent){
        Long userId = userCreateEvent.userId();
        imageService.saveImages(userCreateEvent.images(), userId);
    }
}
