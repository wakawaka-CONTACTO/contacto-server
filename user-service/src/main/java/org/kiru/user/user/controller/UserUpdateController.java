package org.kiru.user.user.controller;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.kiru.core.user.user.domain.User;
import org.kiru.user.auth.argumentresolve.UserId;
import org.kiru.user.user.dto.request.UserUpdateDto;
import org.kiru.user.user.dto.request.UserUpdatePwdDto;
import org.kiru.user.user.dto.response.UpdatePwdResponse;
import org.kiru.user.user.dto.response.UserWithAdditionalInfoResponse;
import org.kiru.user.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserUpdateController {

  private final UserService userService;


  @PutMapping(value = "/me", consumes = {"multipart/form-data"})
  public ResponseEntity<UserWithAdditionalInfoResponse> updateUser(
      @UserId Long userId,
      @Valid @ModelAttribute UserUpdateDto updatedUser,
      // 새로 업로드할 이미지와 각 이미지의 순서를 나타내는 배열
      @RequestParam(required = false) MultipartFile[] portfolioImages,
      @RequestParam(required = false) int[] newImageKeys,
      // 기존 이미지 URL과 각 이미지의 순서를 나타내는 배열
      @RequestParam(required = false) List<String> existingPortfolioImageUrls,
      @RequestParam(required = false) int[] existingImageKeys
  ) {
    // portfolio 맵이 없다면 초기화
    if (updatedUser.getPortfolio() == null) {
      updatedUser.setPortfolio(new HashMap<>());
    }

    // 두 그룹의 키가 중복되지 않는지 검증
    validateImageKeys(newImageKeys, existingImageKeys);

    // 새 이미지와 기존 이미지를 각각 portfolio 맵에 추가
    addNewImages(updatedUser, portfolioImages, newImageKeys);
    addExistingImages(updatedUser, existingPortfolioImageUrls, existingImageKeys);

    User user = userService.updateUser(userId, updatedUser);
    return ResponseEntity.ok(UserWithAdditionalInfoResponse.of(user));
  }

  // 새 이미지와 기존 이미지의 키 중복 여부를 검증하는 메서드
  private void validateImageKeys(int[] newImageKeys, int[] existingImageKeys) {
    Set<Integer> allKeys = new HashSet<>();
    if (newImageKeys != null) {
      for (int key : newImageKeys) {
        if (!allKeys.add(key)) {
          throw new IllegalArgumentException("새로운 이미지 키 중복: " + key);
        }
      }
    }
    if (existingImageKeys != null) {
      for (int key : existingImageKeys) {
        if (!allKeys.add(key)) {
          throw new IllegalArgumentException("새로운 이미지와 기존 이미지의 키가 중복됩니다: " + key);
        }
      }
    }
  }

  // 새 이미지와 해당 키를 portfolio 맵에 추가하는 메서드
  private void addNewImages(UserUpdateDto updatedUser, MultipartFile[] portfolioImages, int[] newImageKeys) {
    if (portfolioImages != null && newImageKeys != null) {
      if (portfolioImages.length != newImageKeys.length) {
        throw new IllegalArgumentException("새로운 이미지와 키의 개수가 일치하지 않습니다.");
      }
      for (int i = 0; i < portfolioImages.length; i++) {
        updatedUser.getPortfolio().put(newImageKeys[i], portfolioImages[i]);
      }
    }
  }

  // 기존 이미지 URL과 해당 키를 portfolio 맵에 추가하는 메서드
  private void addExistingImages(UserUpdateDto updatedUser, List<String> existingPortfolioImageUrls, int[] existingImageKeys) {
    if (existingPortfolioImageUrls != null && existingImageKeys != null) {
      if (existingPortfolioImageUrls.size() != existingImageKeys.length) {
        throw new IllegalArgumentException("기존 이미지 URL과 키의 개수가 일치하지 않습니다.");
      }
      for (int i = 0; i < existingPortfolioImageUrls.size(); i++) {
        updatedUser.getPortfolio().put(existingImageKeys[i], existingPortfolioImageUrls.get(i));
      }
    }
  }


  @PatchMapping(value = "/me/pwd")
  public ResponseEntity<UpdatePwdResponse> updateUserPwd(
      @RequestBody UserUpdatePwdDto userUpdatePwdDto) {
    Boolean isSuccess = userService.updateUserPwd(userUpdatePwdDto);
    return ResponseEntity.ok(new UpdatePwdResponse(isSuccess));
  }
}
