package org.kiru.user.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.kiru.core.user.talent.domain.Talent.TalentType;

@Getter
@AllArgsConstructor
@Builder
public class UserUpdateDto {
    @Pattern(regexp = "^[A-Za-z0-9가-힣]{2,20}$",
            message = "이름은 2-20자의 영문자, 숫자, 한글만 가능합니다")
    private final String username;
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private final String email;
    private final String description;
    private final String instagramId;
    private final String webUrl;
    private final List<Integer> userPurposes;
    private final List<TalentType> userTalents;
    private final Map<Integer, Object> portfolioImages;
}