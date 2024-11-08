package org.kiru.user.user.service;


import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.kiru.core.chatroom.domain.ChatRoom;
import org.kiru.core.talent.entity.UserTalent;
import org.kiru.core.user.domain.User;
import org.kiru.core.userPortfolioImg.domain.UserPortfolio;
import org.kiru.core.userPortfolioImg.entity.UserPortfolioImg;
import org.kiru.core.userPurpose.domain.PurposeType;
import org.kiru.core.userPurpose.entity.UserPurpose;
import org.kiru.user.exception.EntityNotFoundException;
import org.kiru.user.exception.code.FailureCode;
import org.kiru.user.user.api.ChatApiClient;
import org.kiru.user.user.repository.UserPortfolioRepository;
import org.kiru.user.user.repository.UserPurposeRepository;
import org.kiru.user.user.repository.UserRepository;
import org.kiru.user.user.repository.UserTalentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserPurposeRepository userPurposeRepository;
    private final UserTalentRepository userTalentRepository;
    private final UserPortfolioRepository userPortfolioRepository;
    private final ChatApiClient chatApiClient;


    public User getUserFromIdToMainPage(Long userId) {
        User user = User.of(userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(FailureCode.ENTITY_NOT_FOUND))
        );
        return getUserDetails(user);
    }

    public List<ChatRoom> getUserChatRooms(Long userId) {
        return chatApiClient.getUserChatRooms(userId);
    }

    public ChatRoom getUserChatRoom(Long roomId, Long userId) {
        return chatApiClient.getRoom(roomId, userId);
    }

    private User getUserDetails(User user) {
        Long userId = user.getId();
        List<PurposeType> userPurposes = userPurposeRepository.findAllByUserId(userId).stream()
                .map(UserPurpose::getPurposeType).toList();
        List<UserTalent> userTalents = userTalentRepository.findAllByUserId(userId);
        List<UserPortfolioImg> userPortfolioImgs = userPortfolioRepository.findAllByUserId(userId);
        Long portfolioId = userPortfolioImgs.getFirst().getId();
        userPortfolioImgs.sort(Comparator.comparing(UserPortfolioImg::getSequence));
        UserPortfolio userPortfolio = UserPortfolio.builder().portfolioId(portfolioId)
                .portfolioImages(userPortfolioImgs.stream().map(UserPortfolioImg::getPortfolioImageUrl).toList())
                .userId(userId).build();
        user.userPurposes(userPurposes);
        user.userTalents(userTalents);
        user.userPortfolio(userPortfolio);
        return user;
    }
}
