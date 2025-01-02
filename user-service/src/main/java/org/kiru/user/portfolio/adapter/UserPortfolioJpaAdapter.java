package org.kiru.user.portfolio.adapter;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kiru.user.portfolio.dto.res.UserPortfolioResDto;
import org.kiru.user.portfolio.repository.UserPortfolioRepository;
import org.kiru.user.portfolio.service.out.GetUserPortfoliosQuery;
import org.kiru.user.userlike.service.out.GetMatchedUserPortfolioQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Repository
@Slf4j
public class UserPortfolioJpaAdapter implements GetUserPortfoliosQuery , GetMatchedUserPortfolioQuery {
    private final UserPortfolioRepository userPortfolioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UserPortfolioResDto> findAllPortfoliosByUserIds(List<Long> userIds) {
        Map<Long, Integer> userIdOrderMap = userIds.stream()
                .collect(Collectors.toMap(id -> id, userIds::indexOf));
        List<UserPortfolioResDto> userPortfolioResDtos = userPortfolioRepository.findAllPortfoliosByUserIds(userIds)
                .stream().map(portfolio -> UserPortfolioResDto.of(
                        (Long) portfolio[0], // portfolioId
                        (Long) portfolio[1], // userId
                        (String) portfolio[2], // username
                        (String) portfolio[3]
                )).toList();
        return userPortfolioResDtos.stream()
                .sorted(Comparator.comparing(dto -> userIdOrderMap.get(dto.getUserId())))
                .toList();
    }

    public List<UserPortfolioResDto> findByUserIds(List<Long> userIds) {
        return userPortfolioRepository.findAllPortfoliosByUserIds(userIds)
                .stream().map(portfolio -> UserPortfolioResDto.of(
                        (Long) portfolio[0], // portfolioId
                        (Long) portfolio[1], // userId
                        (String) portfolio[2], // username
                        (String) portfolio[3]
                )).toList();
    }
}