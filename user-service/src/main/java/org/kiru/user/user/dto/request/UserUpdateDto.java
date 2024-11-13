package org.kiru.user.user.dto.request;

import jakarta.validation.constraints.Email;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.kiru.core.user.talent.domain.Talent.TalentType;

import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class UserUpdateDto {
    private String username;
    @Email
    private String email;
    private String description;
    private String instagramId;
    private String webUrl;
    private String password;
    private List<Integer> userPurposes;
    private List<TalentType> userTalents;
    private Map<Integer, MultipartFile> portfolioImages = new HashMap<>();
}