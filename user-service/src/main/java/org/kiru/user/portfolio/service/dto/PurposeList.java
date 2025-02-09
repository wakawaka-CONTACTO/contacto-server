package org.kiru.user.portfolio.service.dto;

import java.util.List;
import java.util.Objects;
import lombok.Builder;
import org.kiru.core.user.userPurpose.domain.PurposeType;

@Builder
public record PurposeList(
    List<PurposeType> purposeTypes){
    public static PurposeList of(List<PurposeType> purposeTypes) {
        return PurposeList.builder()
                .purposeTypes(purposeTypes.stream().filter(Objects::nonNull).toList())
                .build();
    }
}
