package org.kiru.core.chat.chatroom.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import autoparams.AutoSource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.kiru.core.chat.message.domain.Message;
import org.kiru.core.user.userPortfolioItem.domain.UserPortfolioItem;
import org.kiru.core.user.userPortfolioItem.entity.UserPortfolioImg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ChatRoomTest {

    private static final Logger log = LoggerFactory.getLogger(ChatRoomTest.class);
    private ChatRoom chatRoom;

    @BeforeEach
    void setUp() {
        Set<Long> participants = new HashSet<>(List.of(2L));
        chatRoom = ChatRoom.builder().id(1L).chatRoomThumbnail(null)
                .participants(participants).type(ChatRoomType.PRIVATE).build();
    }

    @Test
    void 참가자를_SET_으로_주는경우_제대로_설정되는지_확인() {
        Set<Long> participants = new HashSet<>(List.of(1L, 2L));
        ChatRoom chatRoom = ChatRoom.builder().id(1L).chatRoomThumbnail(null)
                .participants(participants).type(ChatRoomType.PRIVATE).build();
        assertEquals(2, chatRoom.getParticipants().size());
    }

    @Test
    void 메시지_추가시_정상적으로_추가되는지_확인() {
        // given
        List<Message> messages = Arrays.asList(
                Message.builder().content("안녕하세요").senderId(1L).build(),
                Message.builder().content("반갑습니다").senderId(2L).build()
        );

        // when
        chatRoom.addMessage(messages);

        // then
        assertEquals(2, chatRoom.getMessages().size());
        assertEquals("안녕하세요", chatRoom.getMessages().get(0).getContent());
        assertEquals("반갑습니다", chatRoom.getMessages().get(1).getContent());
    }

    @Test
    void 참가자_추가시_정상적으로_추가되는지_확인() {
        // given
        List<Long> participants = Arrays.asList(1L, 2L, 3L);
        // when
        chatRoom.addParticipants(participants);

        // then
        assertEquals(3, chatRoom.getParticipants().size());
        assertTrue(chatRoom.getParticipants().containsAll(participants));
    }

    @Test
    void 채팅방_참가자의_포트폴리오_이미지로_섬네일과_제목_설정() {
        // given
        Map<Long, UserPortfolioItem> userPortfolioImgMap = new HashMap<>();
        UserPortfolioItem userPortfolioItem1 = UserPortfolioImg.of(1L, 1L, "http://example.com/image1.jpg", 1,
                "첫번째 사용자");
        UserPortfolioItem userPortfolioItem2 = UserPortfolioImg.of(2L, 2L, "http://example.com/image2.jpg", 1,
                "두번째 사용자");

        userPortfolioImgMap.put(1L, userPortfolioItem1);
        userPortfolioImgMap.put(2L, userPortfolioItem2);

        // when
        log.warn("ISSS!!! " + chatRoom.getParticipants().toString());
        chatRoom.setThumbnailAndRoomTitle(userPortfolioImgMap);

        // then: participatns Id가 일치하는 포트폴리오 이미지의 제목과 섬네일이 설정되어야 함
        assertEquals(userPortfolioItem2.getItemUrl(), chatRoom.getChatRoomThumbnail());
        assertEquals(userPortfolioItem2.getUserName(), chatRoom.getTitle());
    }

    @Test
    void 채팅방_참가자의_포트폴리오_이미지가_없는_경우() {
        // 준비
        Map<Long, UserPortfolioItem> userPortfolioImgMap = new HashMap<>();
        UserPortfolioItem userPortfolioItem1 = UserPortfolioImg.of(1L, 1L, "http://example.com/image1.jpg", 1,
                "첫번째 사용자");
        UserPortfolioItem userPortfolioItem3 = UserPortfolioImg.of(3L, 3L, "http://example.com/image3.jpg", 1,
                "세번째 사용자");

        userPortfolioImgMap.put(1L, userPortfolioItem1);
        userPortfolioImgMap.put(3L, userPortfolioItem3);
        // 실행
        chatRoom.setThumbnailAndRoomTitle(userPortfolioImgMap);
        // 검증: 매칭되는 포트폴리오 이미지가 없으므로 섬네일과 제목이 null이어야 함
        assertNull(chatRoom.getChatRoomThumbnail());
        assertNull(chatRoom.getTitle());
    }


    @Test
    void 채팅방에_메시지_추가() {
        // 준비
        List<Message> messagesToAdd = Arrays.asList(new Message(), new Message());

        // 실행
        chatRoom.addMessage(messagesToAdd);

        // 검증
        assertEquals(2, chatRoom.getMessages().size());
    }

    @Test
    void 채팅방에_참가자_추가() {
        // 준비
        Long userId = 1L;

        // 실행
        boolean added = chatRoom.addParticipant(userId);

        // 검증
        assertTrue(added);
        assertTrue(chatRoom.getParticipants().contains(userId));
    }
}