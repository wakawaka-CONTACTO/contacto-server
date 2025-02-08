package org.kiru.user.user.adapter;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.kiru.core.user.userPurpose.domain.PurposeType;
import org.kiru.core.user.userPurpose.entity.UserPurpose;
import org.kiru.user.portfolio.service.dto.PurposeList;
import org.kiru.user.portfolio.service.out.UserPurposePort;
import org.kiru.user.user.repository.UserPurposeRepository;
import org.kiru.user.userlike.dto.Longs;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserPurposeAdapter implements UserPurposePort {
    private final UserPurposeRepository userPurposeRepository;

    @Override
    @Cacheable(value = "userPurpose", key = "#purposeTypes+'_'+ #pageable.pageNumber", unless = "#result == null")
    public Longs getMatchingUserIdsByPurpose(List<PurposeType> purposeTypes, Pageable pageable) {
        return new Longs(userPurposeRepository.findUserIdsByPurposeTypesOrderByCount(
                purposeTypes, pageable).getContent().stream().toList());
    }

    @Override
    @Cacheable(value = "userPurposeType", key = "#userId", unless = "#result == null")
    public PurposeList findAllPurposeTypeByUserId(Long userId) {
        return PurposeList.of(userPurposeRepository.findAllByUserId(userId).stream()
                .map(UserPurpose::getPurposeType)
                .toList());
    }
}
