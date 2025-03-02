package org.kiru.user.user.dto.request;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kiru.user.portfolio.dto.req.UpdatePortfolioDto;
import org.springframework.web.multipart.MultipartFile;

class UserUpdateDtoTest {

  private UserUpdateDto userUpdateDto;


  @BeforeEach
  void setUp() {
    userUpdateDto = UserUpdateDto.builder()
        .username("testUser")
        .email("test@example.com")
        .description("This is a test user.")
        .instagramId("test_instagram")
        .webUrl("https://example.com")
        .userPurposes(Arrays.asList(1, 2, 3))
        .userTalents(Collections.emptyList())
        .portfolioImage(new HashMap<>())
        .build();
  }


  @Test
  void testPortfolio_UpdateWithNewAndExistingItems() {
    // Mocking UpdatePortfolioDto 객체 생성
    UpdatePortfolioDto newItem = mock(UpdatePortfolioDto.class);
    UpdatePortfolioDto existingItem = mock(UpdatePortfolioDto.class);

    // 새 아이템인 경우
    when(newItem.isNew()).thenReturn(true);
    when(newItem.getKey()).thenReturn(1);
    when(newItem.getResource()).thenReturn("new-image-url");

    // 기존 아이템인 경우
    when(existingItem.isNew()).thenReturn(false);
    when(existingItem.getKey()).thenReturn(2);
    MultipartFile mockFile = mock(MultipartFile.class);
    when(existingItem.getPortfolio()).thenReturn(mockFile);

    // 포트폴리오 업데이트 실행
    List<UpdatePortfolioDto> portfolioList = Arrays.asList(newItem, existingItem);
    userUpdateDto.portfolio(portfolioList);

    // 결과 검증
    Map<Integer, Object> portfolio = userUpdateDto.getPortfolio();
    assertNotNull(portfolio);
    assertEquals(2, portfolio.size());
    assertEquals("new-image-url", portfolio.get(1));
    assertEquals(mockFile, portfolio.get(2));
  }
}