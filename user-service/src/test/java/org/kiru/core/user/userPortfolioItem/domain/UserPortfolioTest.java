package org.kiru.core.user.userPortfolioItem.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kiru.core.user.userPortfolioItem.entity.UserPortfolioImg;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

class UserPortfolioTest {

    private List<UserPortfolioItem> oneUserPortfolioItems;
    private List<UserPortfolioItem> manyUserPortfolioItems;
    private List<UserPortfolioItem> notSortedUserPortfolioItems;


    @BeforeEach
    void setUp() {
        oneUserPortfolioItems = Arrays.asList(
                UserPortfolioImg.of(1L, 1L, "url1", 1, "user1"),
                UserPortfolioImg.of(1L, 1L, "url2", 2, "user1")
        );

        manyUserPortfolioItems = Arrays.asList(
                UserPortfolioImg.of(1L, 1L, "url1", 1, "user1"),
                UserPortfolioImg.of(1L, 1L, "url2", 2, "user1"),
                UserPortfolioImg.of(2L, 2L, "url3", 1, "user2"),
                UserPortfolioImg.of(2L, 2L, "url4", 2, "user2"),
                UserPortfolioImg.of(3L, 3L, "url5", 1, "user3"),
                UserPortfolioImg.of(3L, 3L, "url6", 2, "user3")
        );

        notSortedUserPortfolioItems = Arrays.asList(
                UserPortfolioImg.of(1L, 1L, "url1", 3, "user1"),
                UserPortfolioImg.of(1L, 1L, "url2", 5, "user1"),
                UserPortfolioImg.of(1L, 1L, "url3", 6, "user2"),
                UserPortfolioImg.of(1L, 1L, "url4", 2, "user2"),
                UserPortfolioImg.of(1L, 1L, "url5", 1, "user3"),
                UserPortfolioImg.of(1L, 1L, "url6", 4, "user3")
        );
    }

    @Test
    void 빈_포트폴리오_아이템_추가_및_업데이트() {
        // 준비
        UserPortfolio userPortfolio = UserPortfolio.of(oneUserPortfolioItems);
        // 실행
        assertEquals(2, userPortfolio.getPortfolioItems().size());
        assertEquals("url1", userPortfolio.getPortfolioItems().get(0).getItemUrl());
        assertEquals("url2", userPortfolio.getPortfolioItems().get(1).getItemUrl());
        assertEquals(1, userPortfolio.getPortfolioItems().get(0).getSequence());
        assertEquals(2, userPortfolio.getPortfolioItems().get(1).getSequence());
        assertEquals(userPortfolio.getUserId(), userPortfolio.getPortfolioItems().getFirst().getUserId());
        assertEquals(userPortfolio.getPortfolioId(), userPortfolio.getPortfolioItems().getLast().getPortfolioId());
    }

    @Test
    void 포트폴리오_아이템_정렬() {
        // 준비
        UserPortfolio userPortfolio = UserPortfolio.of(notSortedUserPortfolioItems);
        // 실행
        userPortfolio.sort();

        // 검증
        assertEquals(1, userPortfolio.getPortfolioItems().get(0).getSequence());
        assertEquals(2, userPortfolio.getPortfolioItems().get(1).getSequence());
        assertEquals(3, userPortfolio.getPortfolioItems().get(2).getSequence());
        assertEquals(4, userPortfolio.getPortfolioItems().get(3).getSequence());
        assertEquals(5, userPortfolio.getPortfolioItems().get(4).getSequence());
        assertEquals(6, userPortfolio.getPortfolioItems().get(5).getSequence());
    }

    @Test
    void 업데이트_아이템_찾기() {
        // 준비
        Map<Integer, MultipartFile> items = new HashMap<>();
        MultipartFile mockFile = new MockMultipartFile("file", "filename.jpg", "image/jpeg", new byte[0]);
        MultipartFile mockFile2 = new MockMultipartFile("file", "filename.jpg", "image/jpeg", new byte[0]);
        MultipartFile mockFile3 = new MockMultipartFile("file", "filename.jpg", "image/jpeg", new byte[0]);
        String mockString = "string";
        String mockString2 = "string2";
        items.put(1, mockFile);
//        items.put(2, mockString);
        items.put(3, mockFile2);
//        items.put(4, mockString2);
        items.put(5, mockFile3);
        // 실행
        Map<Integer, MultipartFile> updateItems = UserPortfolio.findUpdateItem(items);

        // 검증
        assertEquals(3, updateItems.size());
        assertTrue(updateItems.containsKey(1));
        assertTrue(updateItems.containsKey(3));
        assertTrue(updateItems.containsKey(5));

        assertEquals(mockFile, updateItems.get(1));
        assertEquals(mockFile2, updateItems.get(3));
        assertEquals(mockFile3, updateItems.get(5));

    }

    private void assertPortfolioItemEquals(UserPortfolioItem expected, UserPortfolioItem actual) {
        assertEquals(expected.getUserId(), actual.getUserId());
        assertEquals(expected.getPortfolioId(), actual.getPortfolioId());
        assertEquals(expected.getItemUrl(), actual.getItemUrl());
        assertEquals(expected.getSequence(), actual.getSequence());
        assertEquals(expected.getUserName(), actual.getUserName());
    }

    @Test
    void 사용자ID와_포트폴리오_아이템_맵_생성() {
        Map<Long, UserPortfolioItem> expectedItems = manyUserPortfolioItems.stream()
                .filter(item -> item.getSequence() == 1)
                .collect(Collectors.toMap(
                        UserPortfolioItem::getUserId,
                        item -> item
                ));

        // 실행
        Map<Long, UserPortfolioItem> resultMap = UserPortfolio.getUserIdAndUserPortfolioItemMap(manyUserPortfolioItems);

        // 값 비교
        assertEquals(expectedItems.keySet(), resultMap.keySet());
        expectedItems.forEach((userId, expectedItem) ->
                assertPortfolioItemEquals(expectedItem, resultMap.get(userId)));
    }

    @Test
    void 포트폴리오_아이템_추가_및_업데이트_테스트() {
        // 1. Portfolio의 List가 비어있을 때 들어온 값으로 다 바뀌는지
        UserPortfolio emptyPortfolio = UserPortfolio.withUserId(1L);
        List<UserPortfolioItem> newItems = Arrays.asList(
                UserPortfolioImg.of(1L, 1L, "url1", 1, "user1"),
                UserPortfolioImg.of(1L, 1L, "url2", 2, "user1")
        );

        emptyPortfolio.addOrUpdatePortfolioItems(newItems);

        assertEquals(2, emptyPortfolio.getPortfolioItems().size());
        assertEquals("url1", emptyPortfolio.getPortfolioItems().get(0).getItemUrl());
        assertEquals("url2", emptyPortfolio.getPortfolioItems().get(1).getItemUrl());

        // 2. Portfolio가 안 비어있을 때 값이 들어온 값의 List랑 크기가 일치하고 값이 다 일치하는지
        UserPortfolio filledPortfolio = UserPortfolio.of(newItems);
        List<UserPortfolioItem> updateItems = Arrays.asList(
                UserPortfolioImg.of(1L, 1L, "url4", 2, "user1"),
                UserPortfolioImg.of(1L, 1L, "url5", 3, "user1")
        );

        filledPortfolio.addOrUpdatePortfolioItems(updateItems);

        assertEquals(3, filledPortfolio.getPortfolioItems().size());
        assertEquals("url1", filledPortfolio.getPortfolioItems().get(0).getItemUrl());
        assertEquals("url4", filledPortfolio.getPortfolioItems().get(1).getItemUrl());
        assertEquals("url5", filledPortfolio.getPortfolioItems().get(2).getItemUrl());

        // 3. 각각 List가 sequence 값이 순서대로 증가하게 되어 있는지
        filledPortfolio.sort(); // 정렬 메서드 호출
        assertEquals(1, filledPortfolio.getPortfolioItems().get(0).getSequence());
        assertEquals(2, filledPortfolio.getPortfolioItems().get(1).getSequence());
        assertEquals(3, filledPortfolio.getPortfolioItems().get(2).getSequence());
    }

    @Test
    void 포트폴리오_Id로_생성시_ImmutableCollection이_들어올경우_update_add등이_동작하는지_여부() {
        // 1. Portfolio의 List가 비어있을 때 들어온 값으로 다 바뀌는지
        UserPortfolio emptyPortfolio = UserPortfolio.withUserId(1L);
        List<UserPortfolioItem> newItems = List.of(
                UserPortfolioImg.of(1L, 1L, "url1", 1, "user1"),
                UserPortfolioImg.of(1L, 1L, "url2", 2, "user1")
        );

        emptyPortfolio.addOrUpdatePortfolioItems(newItems);

        assertEquals(2, emptyPortfolio.getPortfolioItems().size());
        assertEquals("url1", emptyPortfolio.getPortfolioItems().get(0).getItemUrl());
        assertEquals("url2", emptyPortfolio.getPortfolioItems().get(1).getItemUrl());

        // 2. Portfolio가 안 비어있을 때 값이 들어온 값의 List랑 크기가 일치하고 값이 다 일치하는지
        UserPortfolio filledPortfolio = UserPortfolio.of(newItems);
        List<UserPortfolioItem> updateItems = List.of(
                UserPortfolioImg.of(1L, 1L, "url4", 2, "user1"),
                UserPortfolioImg.of(1L, 1L, "url5", 3, "user1")
        );

        filledPortfolio.addOrUpdatePortfolioItems(updateItems);

        assertEquals(3, filledPortfolio.getPortfolioItems().size());
        assertEquals("url1", filledPortfolio.getPortfolioItems().get(0).getItemUrl());
        assertEquals("url4", filledPortfolio.getPortfolioItems().get(1).getItemUrl());
        assertEquals("url5", filledPortfolio.getPortfolioItems().get(2).getItemUrl());

        // 3. 각각 List가 sequence 값이 순서대로 증가하게 되어 있는지
        filledPortfolio.sort(); // 정렬 메서드 호출
        assertEquals(1, filledPortfolio.getPortfolioItems().get(0).getSequence());
        assertEquals(2, filledPortfolio.getPortfolioItems().get(1).getSequence());
        assertEquals(3, filledPortfolio.getPortfolioItems().get(2).getSequence());
    }

    @Test
    void 포트폴리오Item으로_생성시_ImmutableCollection이_들어올경우_update_add등이_동작하는지_여부() {
        // 1. Portfolio의 List가 비어있을 때 들어온 값으로 다 바뀌는지
        List<UserPortfolioItem> newItems = List.of(
                UserPortfolioImg.of(1L, 1L, "url1", 1, "user1"),
                UserPortfolioImg.of(1L, 1L, "url2", 2, "user1")
        );
        UserPortfolio myPortfolio = UserPortfolio.of(newItems);

        assertEquals(1, myPortfolio.getUserId());
        assertEquals(1, myPortfolio.getPortfolioId());
        // 2. Portfolio가 안 비어있을 때 값이 들어온 값의 List랑 크기가 일치하고 값이 다 일치하는지

        List<UserPortfolioItem> updateItems = List.of(
                UserPortfolioImg.of(1L, 1L, "url4", 2, "user1"),
                UserPortfolioImg.of(1L, 1L, "url5", 3, "user1")
        );

        myPortfolio.addOrUpdatePortfolioItems(updateItems);

        assertEquals(3, myPortfolio.getPortfolioItems().size());
        assertEquals("url1", myPortfolio.getPortfolioItems().get(0).getItemUrl());
        assertEquals("url4", myPortfolio.getPortfolioItems().get(1).getItemUrl());
        assertEquals("url5", myPortfolio.getPortfolioItems().get(2).getItemUrl());

        // 3. 각각 List가 sequence 값이 순서대로 증가하게 되어 있는지
        myPortfolio.sort(); // 정렬 메서드 호출
        assertEquals(1, myPortfolio.getPortfolioItems().get(0).getSequence());
        assertEquals(2, myPortfolio.getPortfolioItems().get(1).getSequence());
        assertEquals(3, myPortfolio.getPortfolioItems().get(2).getSequence());
    }
} 