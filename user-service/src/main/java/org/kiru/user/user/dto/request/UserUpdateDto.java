package org.kiru.user.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.HashMap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.validator.constraints.URL;
import org.kiru.core.user.talent.domain.Talent.TalentType;

import java.util.List;
import java.util.Map;
//import org.kiru.user.portfolio.dto.req.PortfolioImagesRequest;
import org.springframework.web.multipart.MultipartFile;

@Getter
@AllArgsConstructor
@Builder
public class UserUpdateDto {
    @Pattern(regexp = "^[A-Za-z0-9가-힣]{2,20}$",
            message = "이름은 2-20자의 영문자, 숫자, 한글만 가능합니다")
    private final String username;
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private final String email;

    @Size(max = 1000, message = "소개는 최대 1000자까지 입력 가능합니다")
    private final String description;
    private final String instagramId;

    @URL(message = "올바른 URL 형식이 아닙니다")
    @Size(max = 255, message = "URL은 255자를 초과할 수 없습니다")
    private final String webUrl;

    @NotNull
    @Size(max = 5, message = "목적은 최대 5개까지 선택 가능합니다")
    private final List<Integer> userPurposes;
    @NotNull
    private final List<TalentType> userTalents;

    @Size(max = 10, message = "포트폴리오는 최대 10개까지 등록 가능합니다")
    private Map<Integer, Object> portfolio;

    public void setPortfolioImages(MultipartFile[] portfolioImages, int[] keys) {
        this.portfolio = new HashMap<>();
        if(portfolioImages == null) return;
        for (int i = 0; i < portfolioImages.length; i++) {
            this.portfolio.put(keys[i], portfolioImages[i]);
        }
    }

}