package org.kiru.user.portfolio.adapter;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.kiru.user.portfolio.service.out.GetRecommendUserIdsQuery;
import org.kiru.user.user.repository.UserRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class UserRecommendAdapter implements GetRecommendUserIdsQuery {
    private final UserRepository userRepository;
    public UserRecommendAdapter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<Long> findRecommendedUserIds(Long userId, Pageable pageable) {
        int size = pageable.getPageSize();
        int page = pageable.getPageNumber();
        return userRepository.findRecommendedUserIds(userId, size, page*size);
    }
}
