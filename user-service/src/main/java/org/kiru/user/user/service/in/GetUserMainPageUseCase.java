package org.kiru.user.user.service.in;

import org.kiru.core.user.user.domain.User;

public interface GetUserMainPageUseCase {
    User getUserFromIdToMainPage(Long userId);
}
