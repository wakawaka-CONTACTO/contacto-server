package org.kiru.user.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.kiru.core.user.talent.domain.Talent.TalentType;

@Getter
@Setter
@NoArgsConstructor
public class UserUpdateDto {
    @Pattern(regexp = "^[A-Za-z0-9가-힣]{2,20}$",
            message = "이름은 2-20자의 영문자, 숫자, 한글만 가능합니다")
    private String username;

    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;
    private String description;
    private String instagramId;
    private String webUrl;
    private List<Integer> userPurposes;
    private List<TalentType> userTalents;
    private Map<Integer, Object> portfolioImages = new HashMap<>();
}