package org.kiru.core.user.userPortfolioItem.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserPortfolio {
    private Long portfolioId;
    private Long userId;
    @Builder.Default
    private final List<UserPortfolioItem> portfolioItems = new ArrayList<>();

//        return new UserPortfolio(portfolioItems == null ? new ArrayList<>() : portfolioItems);
public static UserPortfolio of(List<UserPortfolioItem> items) {
    Long portfolioId = null;
    Long userId = null;

    // items가 비어 있지 않으면 portfolioId와 userId 설정
    if (!items.isEmpty()) {
        portfolioId = items.get(0).getPortfolioId(); // getFirst() 대신 첫 번째 아이템 사용
        userId = items.get(0).getUserId();
    }

    return UserPortfolio.builder()
        .portfolioId(portfolioId)
        .userId(userId)
        .portfolioItems(new ArrayList<>(items)) // 수정 가능한 리스트로 설정
        .build();
}

    public static UserPortfolio withUserId(Long userId) {
        return UserPortfolio.builder()
                .portfolioId(null)
                .userId(userId)
                .portfolioItems(new ArrayList<>())
                .build();
    }

    public void sort() {
        if(this.portfolioItems.isEmpty()) return;
        this.portfolioItems.sort(Comparator.comparing(UserPortfolioItem::getSequence));
    }

    public static Map<Integer, MultipartFile> findUpdateItem(Map<Integer, Object> items) {
        Map<Integer, MultipartFile> updateItem = new HashMap<>();
        if (items != null) {
            for (Entry<Integer, Object> entry : items.entrySet()) {
                Integer sequence = entry.getKey();
                Object updatedImg = entry.getValue();
                if (updatedImg instanceof MultipartFile file) {
                    updateItem.put(sequence, file);
                }
            }
        }
        return updateItem;
    }

    public void addOrUpdatePortfolioItems(List<UserPortfolioItem> updateItems) {
        if (updateItems == null) {
            throw new IllegalArgumentException("updateItem이 Null입니다.");
        }
        if (updateItems.stream().anyMatch(item -> item.getSequence() > 10)) {
            throw new IllegalArgumentException("Sequence number는 1~10까지 가능합니다.");
        }
        if (this.portfolioItems.isEmpty()) {
            this.portfolioItems.addAll(updateItems);
        } else {
            for (UserPortfolioItem newItem : updateItems) {
                int index = newItem.getSequence() - 1;
                if(index < 0) index = 0;
                while (index >= this.portfolioItems.size()) {
                    this.portfolioItems.add(null);
                }
                this.portfolioItems.set(index, newItem);
            }
        }
    }

    public static Map<Long, UserPortfolioItem> getUserIdAndUserPortfolioItemMap(List<UserPortfolioItem> userPortfolioItems) {
        return userPortfolioItems.stream()
                .collect(Collectors.toMap(
                        UserPortfolioItem::getUserId,
                        img -> img,
                        (existing, replacement) -> existing.getSequence() < replacement.getSequence() ? existing : replacement
                ));
    }
}
