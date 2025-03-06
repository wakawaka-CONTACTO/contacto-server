package org.kiru.user.user.controller;

import jakarta.validation.Valid;
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
      @RequestParam(required = false) MultipartFile[] portfolioImages,
      @RequestParam(required = false) int[] keys
  ) {
    updatedUser.setPortfolioImages(portfolioImages, keys);
    User user = userService.updateUser(userId, updatedUser);

    return ResponseEntity.ok(UserWithAdditionalInfoResponse.of(user));
  }


  @PatchMapping(value = "/me/pwd")
  public ResponseEntity<UpdatePwdResponse> updateUserPwd(
      @RequestBody UserUpdatePwdDto userUpdatePwdDto) {
    Boolean isSuccess = userService.updateUserPwd(userUpdatePwdDto);
    return ResponseEntity.ok(new UpdatePwdResponse(isSuccess));
  }
}
