package org.kiru.user.portfolio.adapter;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kiru.core.user.userPortfolioItem.domain.UserPortfolioItem;
import org.kiru.core.user.userPortfolioItem.entity.UserPortfolioImg;
import org.kiru.user.portfolio.dto.res.UserPortfolioResDto;
import org.kiru.user.portfolio.repository.UserPortfolioRepository;
import org.kiru.user.portfolio.service.out.GetUserPortfoliosQuery;
import org.kiru.user.portfolio.service.out.SaveUserPortfolioPort;
import org.kiru.user.userlike.service.out.GetMatchedUserPortfolioQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Repository
@Slf4j
public class UserPortfolioJpaAdapter implements GetUserPortfoliosQuery , GetMatchedUserPortfolioQuery,
        SaveUserPortfolioPort {
    private final UserPortfolioRepository userPortfolioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UserPortfolioResDto> findAllPortfoliosByUserIds(List<Long> userIds) {
        Map<Long, Integer> userIdOrderMap = userIds.stream()
                .collect(Collectors.toMap(id -> id, userIds::indexOf));
        List<UserPortfolioResDto> userPortfolioResDtos = userPortfolioRepository.findAllPortfoliosByUserIds(userIds)
                .stream().map(UserPortfolioResDto::of).toList();
        return userPortfolioResDtos.stream()
                .sorted(Comparator.comparing(dto -> userIdOrderMap.get(dto.getUserId())))
                .toList();
    }

    @Override
    public List<UserPortfolioItem> getUserPortfoliosWithMinSequence(List<Long> allParticipantIds) {
        return userPortfolioRepository.findAllByUserIdInWithItemUrlMinSequence(allParticipantIds).stream()
                .map(UserPortfolioImg::toModel)
                .toList();
    }

    public List<UserPortfolioResDto> findByUserIds(List<Long> userIds) {
        return userPortfolioRepository.findAllPortfoliosByUserIds(userIds)
                .stream().map(UserPortfolioResDto::of).toList();
    }

    @Override
    @Transactional
    public void saveAll(List<UserPortfolioItem> userPortfolioItems) {
        userPortfolioRepository.saveAll(userPortfolioItems.stream().map(UserPortfolioImg::toEntity).toList());
    }

    @Override
    public void save(UserPortfolioItem newImage) {
        userPortfolioRepository.save(UserPortfolioImg.toEntity(newImage));
    }
}