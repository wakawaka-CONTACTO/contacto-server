package org.kiru.core.user.userPortfolioItem.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.kiru.core.user.userPortfolioItem.entity.UserPortfolioImg;
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

  public static UserPortfolio of(List<UserPortfolioItem> items) {
    return UserPortfolio.builder()
        .portfolioId(items.isEmpty() ? null : items.getFirst().getPortfolioId())
        .userId(items.isEmpty() ? null : items.getFirst().getUserId())
        .portfolioItems(new ArrayList<>(items))
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
    if (this.portfolioItems.isEmpty()) {
      return;
    }
    List<UserPortfolioItem> mutableItems = new ArrayList<>(this.portfolioItems);
    mutableItems.removeIf(Objects::isNull);
    mutableItems.sort(Comparator.comparing(UserPortfolioItem::getSequence));
    this.portfolioItems.clear();
    this.portfolioItems.addAll(mutableItems);
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

  public static Map<Integer, String> findExistingItem(Map<Integer, Object> items){
    Map<Integer, String> existingItem = new HashMap<>();
    if (items!= null) {
      for (Entry<Integer, Object> entry : items.entrySet()){
        Integer sequance = entry.getKey();
        Object existingImg = entry.getValue();
        if (existingImg instanceof String file){
          existingItem.put(sequance, file);
        }
      }
    }

    return existingItem;
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
        while (index >= this.portfolioItems.size()) {
          this.portfolioItems.add(new UserPortfolioImg());
        }

        this.portfolioItems.set(index, newItem);
      }
    }
  }

  public static Map<Long, UserPortfolioItem> getUserIdAndUserPortfolioItemMap(
      List<UserPortfolioItem> userPortfolioItems) {
    return userPortfolioItems.stream()
        .collect(Collectors.toMap(
            UserPortfolioItem::getUserId,
            img -> img,
            (existing, replacement) -> existing.getSequence() < replacement.getSequence() ? existing
                : replacement
        ));
  }
}
