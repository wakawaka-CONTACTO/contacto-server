package org.kiru.user.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kiru.core.exception.EntityNotFoundException;
import org.kiru.core.user.talent.domain.Talent.TalentType;
import org.kiru.core.user.user.domain.LoginType;
import org.kiru.core.user.user.domain.User;
import org.kiru.core.user.user.entity.UserJpaEntity;
import org.kiru.core.user.userPortfolioItem.domain.UserPortfolio;
import org.kiru.core.user.userPurpose.domain.PurposeType;
import org.kiru.user.auth.jwt.Token;
import org.kiru.user.user.dto.request.UserPurposesReq;
import org.kiru.user.user.dto.request.UserSignUpReq;
import org.kiru.user.user.dto.request.UserTalentsReq;
import org.kiru.user.user.dto.request.UserUpdateDto;
import org.kiru.user.user.repository.UserRepository;
import org.kiru.user.user.service.out.UserQueryWithCache;
import org.kiru.user.user.service.out.UserUpdatePort;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
public class UserServiceUpdateUserTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private UserUpdatePort userUpdateport; // 목적, 재능, 포트폴리오 업데이트 인터페이스

  @Mock
  private UserQueryWithCache userQueryWithCache; // 캐시 업데이트 관련

  @InjectMocks
  private UserService userService; // updateUser 메서드를 포함하는 서비스 클래스


  private UserUpdateDto userUpdateDto;
  final String originalPassword = "originalPassword";


  @BeforeEach
  void setUp() {
    Map<Integer, Object> items = new HashMap<>();
    MultipartFile mockFile = new MockMultipartFile("file", "filename.jpg", "image/jpeg",
        new byte[0]);
    MultipartFile mockFile2 = new MockMultipartFile("file", "filename.jpg", "image/jpeg",
        new byte[0]);
    MultipartFile mockFile3 = new MockMultipartFile("file", "filename.jpg", "image/jpeg",
        new byte[0]);
    String mockString = "string";
    String mockString2 = "string2";
    items.put(1, mockFile);
    items.put(2, mockString);
    items.put(3, mockFile2);
    items.put(4, mockString2);
    items.put(5, mockFile3);
    userUpdateDto = UserUpdateDto.builder()
        .email("test@example.com")
        .username("user1")
        .userPurposes(List.of(1, 2, 3))
        .userTalents(List.of(TalentType.ARCHITECTURE, TalentType.COMPOSE, TalentType.DANCE))
        .portfolio(
            items
        )
//        .password(originalPassword)
        .build();
  }
}
