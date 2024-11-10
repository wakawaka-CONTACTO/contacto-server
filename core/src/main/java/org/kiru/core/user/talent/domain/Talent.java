package org.kiru.core.user.talent.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.EntityNotFoundException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Talent {

    @Getter
    public enum TalentCategory {
        DESIGN("디자인"),
        ART_CRAFT("예술/공예"),
        MEDIA_CONTENT("미디어/콘텐츠");

        private final String displayName;

        TalentCategory(String displayName) {
            this.displayName = displayName;
        }

        @JsonCreator
        public static TalentCategory fromString(String value) {
            for (TalentCategory category : TalentCategory.values()) {
                if (category.displayName.equals(value) || category.name().equals(value)) {
                    return category;
                }
            }
            throw new EntityNotFoundException("TalentCategory not found for value: " + value);
        }

        @JsonValue
        public String getDisplayName() {
            return displayName;
        }
    }

    @Getter
    public enum TalentType {
        INDUSTRIAL("산업 디자인", TalentCategory.DESIGN),
        GRAPHIC("그래픽 디자인", TalentCategory.DESIGN),
        FASHION("패션 디자인", TalentCategory.DESIGN),
        UX_UI("UX/UI 디자인", TalentCategory.DESIGN),
        BRANDING("브랜딩", TalentCategory.DESIGN),
        MOTION_GRAPHIC("모션 그래픽", TalentCategory.DESIGN),
        ANIMATION("애니메이션", TalentCategory.DESIGN),
        ILLUSTRATION("일러스트레이션", TalentCategory.DESIGN),
        INTERIOR("인테리어 디자인", TalentCategory.DESIGN),
        ARCHITECTURE("건축 디자인", TalentCategory.DESIGN),
        TEXTILE("텍스타일", TalentCategory.DESIGN),
        FABRIC_PRODUCT("패브릭 제품", TalentCategory.DESIGN),
        STYLING("스타일링", TalentCategory.DESIGN),
        BAG_DESIGN("가방 디자인", TalentCategory.DESIGN),
        SHOES_DESIGN("신발 디자인", TalentCategory.DESIGN),
        PAINTING("회화", TalentCategory.ART_CRAFT),
        RIDICULE("조소", TalentCategory.ART_CRAFT),
        KINETIC("키네틱 아트", TalentCategory.ART_CRAFT),
        CERAMICS("도자기", TalentCategory.ART_CRAFT),
        WOOD("목공", TalentCategory.ART_CRAFT),
        JEWEL("주얼리", TalentCategory.ART_CRAFT),
        METAL("금속 공예", TalentCategory.ART_CRAFT),
        GLASS("유리 공예", TalentCategory.ART_CRAFT),
        PRINTMAKING("판화", TalentCategory.ART_CRAFT),
        AESTHETICS("미학", TalentCategory.ART_CRAFT),
        TUFTING("터프팅", TalentCategory.ART_CRAFT),
        POET("시인", TalentCategory.MEDIA_CONTENT),
        WRITING("글쓰기", TalentCategory.MEDIA_CONTENT),
        PHOTO("사진", TalentCategory.MEDIA_CONTENT),
        ADVERTISING("광고", TalentCategory.MEDIA_CONTENT),
        SCENARIO("시나리오", TalentCategory.MEDIA_CONTENT),
        COMPOSE("작곡", TalentCategory.MEDIA_CONTENT),
        DIRECTOR("감독", TalentCategory.MEDIA_CONTENT),
        DANCE("춤", TalentCategory.MEDIA_CONTENT),
        SING("노래", TalentCategory.MEDIA_CONTENT),
        MUSICAL("뮤지컬", TalentCategory.MEDIA_CONTENT),
        COMEDY("코미디", TalentCategory.MEDIA_CONTENT),
        ACT("연기", TalentCategory.MEDIA_CONTENT),
        PRODUCTION("제작", TalentCategory.MEDIA_CONTENT);

        private final String displayName;
        private final TalentCategory category;

        TalentType(String displayName, TalentCategory category) {
            this.displayName = displayName;
            this.category = category;
        }

        @JsonCreator
        public static TalentType fromString(String value) {
            for (TalentType type : TalentType.values()) {
                if (type.displayName.equals(value) || type.name().equals(value)) {
                    return type;
                }
            }
            throw new EntityNotFoundException("TalentType not found for value: " + value);
        }

        @JsonValue
        public String getDisplayName() {
            return displayName;
        }
    }
}
