package org.kiru.user.user.controller;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.kiru.core.user.user.domain.User;
import org.kiru.user.auth.argumentresolve.UserId;
import org.kiru.user.portfolio.dto.req.AddMultipartFileDto;
import org.kiru.user.portfolio.dto.req.PortfolioImagesRequest;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserUpdateController {

  private final UserService userService;

  @PutMapping(value = "/me", consumes = {"multipart/form-data"})
  public ResponseEntity<UserWithAdditionalInfoResponse> updateUser(
      @UserId Long userId,
      @Valid @ModelAttribute UserUpdateDto updatedUser,
      @ModelAttribute PortfolioImagesRequest portfolioImagesRequest // DTO 사용
  ) {
    List<AddMultipartFileDto> portfolioImages = (portfolioImagesRequest != null)
        ? portfolioImagesRequest.getPortfolioImages()
        : Collections.emptyList(); // null 방지

    updatedUser.setPortfolioImages(portfolioImages);
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
