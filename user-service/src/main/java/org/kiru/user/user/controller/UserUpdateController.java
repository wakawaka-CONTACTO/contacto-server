package org.kiru.user.user.controller;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserUpdateController {

  private final UserService userService;


  @PutMapping(value = "/me", consumes = {"multipart/form-data"})
  public ResponseEntity<UserWithAdditionalInfoResponse> updateUser(
      @UserId Long userId,
      @Valid @ModelAttribute UserUpdateDto updatedUser,
      @RequestParam(required = false) MultipartFile[] newPortfolioImages,
      @RequestParam(required = false) List<Integer> newImageKeys,
      @RequestParam(required = false) List<String> existingPortfolioImageUrls,
      @RequestParam(required = false) List<Integer> existingImageKeys
  ) {
    if (updatedUser.getPortfolio() == null) {
      updatedUser.setPortfolio(new HashMap<>());
    }
    validateImageKeys(newImageKeys, existingImageKeys);

    addNewImages(updatedUser, newPortfolioImages, newImageKeys);
    addExistingImages(updatedUser, existingPortfolioImageUrls, existingImageKeys);

    User user = userService.updateUser(userId, updatedUser);
    return ResponseEntity.ok(UserWithAdditionalInfoResponse.of(user));
  }

  private void validateImageKeys(List<Integer> newImageKeys, List<Integer> existingImageKeys) {
    Set<Integer> allKeys = new HashSet<>();
    if (newImageKeys != null) {
      for (int key : newImageKeys) {
        if (!allKeys.add(key)) {
          log.error("[Profile Update] duplicated Sequance Key={}", key);
          throw new IllegalArgumentException("새로운 이미지 키 중복: " + key);
        }
      }
    }
    if (existingImageKeys != null) {
      for (int key : existingImageKeys) {
        if (!allKeys.add(key)) {
          log.error("[Profile Update] duplicated Sequance Key={}", key);
          throw new IllegalArgumentException("새로운 이미지와 기존 이미지의 키가 중복됩니다: " + key);
        }
      }
    }
  }

  private void addNewImages(UserUpdateDto updatedUser, MultipartFile[] portfolioImages, List<Integer> newImageKeys) {
    if (portfolioImages != null && newImageKeys != null) {
      log.debug("[Profile Update] new Image counts={}", portfolioImages.length);
      if (portfolioImages.length != newImageKeys.size()) {
        log.error("[Profile Update] new Image File counts{} is not Equals new Sequance Key count={}", portfolioImages.length, newImageKeys.size());
        throw new IllegalArgumentException("새로운 이미지와 키의 개수가 일치하지 않습니다.");
      }
      for (int i = 0; i < portfolioImages.length; i++) {
        updatedUser.putPortfolio(newImageKeys.get(i), portfolioImages[i]);
      }
    }
  }

  private void addExistingImages(UserUpdateDto updatedUser, List<String> existingPortfolioImageUrls, List<Integer> existingImageKeys) {
    if (existingPortfolioImageUrls != null && existingImageKeys != null) {
      log.debug("[Profile Update] existed Image counts={}", existingPortfolioImageUrls.size());
      if (existingPortfolioImageUrls.size() != existingImageKeys.size()) {
        log.error("[Profile Update] existed Image File counts{} is not Equals Sequance Key counts{}", existingPortfolioImageUrls.size(), existingImageKeys.size());
        throw new IllegalArgumentException("기존 이미지 URL과 키의 개수가 일치하지 않습니다.");
      }
      for (int i = 0; i < existingPortfolioImageUrls.size(); i++) {
        updatedUser.putPortfolio(existingImageKeys.get(i), existingPortfolioImageUrls.get(i));
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
