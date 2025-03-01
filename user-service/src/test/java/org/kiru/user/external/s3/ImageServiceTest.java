package org.kiru.user.external.s3;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kiru.core.user.userPortfolioItem.domain.UserPortfolio;
import org.kiru.core.user.userPortfolioItem.domain.UserPortfolioItem;
import org.kiru.user.portfolio.common.PortfolioIdGenerator;
import org.kiru.user.portfolio.service.out.SaveUserPortfolioPort;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    private String path="path/";

    @InjectMocks
    private String cachePath="cachePath/";

    @InjectMocks
    private ImageService imageService;

    @Mock
    private PortfolioIdGenerator portfolioIdGenerator;

    @Mock
    private S3Service s3Service;
    private List<MultipartFile> images;

    @Mock
    SaveUserPortfolioPort saveUserPortfolioPort;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(imageService, "cachePath", "https://test-cloudfront.net/");

        MultipartFile mockFile1 = new MockMultipartFile("file", "filename.jpg", "image/jpeg", new byte[0]);
        MultipartFile mockFile2 = new MockMultipartFile("file", "filename.jpg", "image/jpeg", new byte[0]);
        MultipartFile mockFile3 = new MockMultipartFile("file", "filename.jpg", "image/jpeg", new byte[0]);
        MultipartFile mockFile4 = new MockMultipartFile("file", "filename.jpg", "image/jpeg", new byte[0]);
        MultipartFile mockFile5 = new MockMultipartFile("file", "filename.jpg", "image/jpeg", new byte[0]);
        MultipartFile mockFile6 = new MockMultipartFile("file", "filename.jpg", "image/jpeg", new byte[0]);
        MultipartFile mockFile7 = new MockMultipartFile("file", "filename.jpg", "image/jpeg", new byte[0]);
        MultipartFile mockFile8 = new MockMultipartFile("file", "filename.jpg", "image/jpeg", new byte[0]);
        MultipartFile mockFile9 = new MockMultipartFile("file", "filename.jpg", "image/jpeg", new byte[0]);
        images = List.of(mockFile1, mockFile2, mockFile3, mockFile4, mockFile5, mockFile6, mockFile7, mockFile8, mockFile9);

    }
    @Test
    @DisplayName("유저 회원가입시에 이미지를 저장한다.")
    void saveImages() throws IOException {
        //given
        when(s3Service.uploadImage(any(), any())).thenReturn("imageUrl.png");
        when(portfolioIdGenerator.generatePortfolioId()).thenReturn(12345L);

        //when
        List<UserPortfolioItem> saveImages = imageService.saveImages(images, 1L, "userName");
        //then
        saveImages.sort(Comparator.comparing(UserPortfolioItem::getSequence));
        assertThat(saveImages).hasSize(9);
        for(int i=0;i<9;i++){
            assertThat(saveImages.get(i).getUserId()).isEqualTo(1L);
            assertThat(saveImages.get(i).getItemUrl()).isEqualTo("https://test-cloudfront.net/imageUrl.png");
            assertThat(saveImages.get(i).getUserName()).isEqualTo("userName");
            assertThat(saveImages.get(i).getSequence()).isEqualTo(i+1);
            assertThat(saveImages.get(i).getPortfolioId()).isEqualTo(12345L);
        }
        verify(s3Service, times(9)).uploadImage(any(), any());
        verify(portfolioIdGenerator, times(1)).generatePortfolioId(); // private 메소드 호출 검증

    }


    @Test
    @DisplayName("Index를 가진 이미지+텍스트 객체가 들어올때 새롭게 업데이트된 이미지 파일만 저장한다")
    void saveImagesS3WithSequence() throws IOException {
        // given
        Map<Integer, MultipartFile> changedPortfolioImages =  new HashMap<>();
//        changedPortfolioImages.put(1, "1.png");
        changedPortfolioImages.put(2, images.get(0));
//        changedPortfolioImages.put(3, "2.png");
        changedPortfolioImages.put(4, images.get(1));
        changedPortfolioImages.put(7, images.get(2));
        List<Integer> changeIndex = List.of(2, 4, 7);
        UserPortfolio userPortfolio = UserPortfolio.withUserId(1L);

        when(s3Service.uploadImage(any(), any())).thenReturn("imageUrl.png");
        when(portfolioIdGenerator.generatePortfolioId()).thenReturn(12345L);

        // when
        List<UserPortfolioItem> result = imageService.saveImagesS3WithSequence(changedPortfolioImages, userPortfolio, "userName");
        // then
        result.sort(Comparator.comparing(UserPortfolioItem::getSequence));
        assertThat(result).hasSize(3); // 변경된 이미지 3개

        for (int i = 0; i < 3; i++) {
            assertThat(result.get(i).getUserId()).isEqualTo(1L);
            assertThat(result.get(i).getPortfolioId()).isEqualTo(12345L);
            assertThat(result.get(i).getItemUrl()).isEqualTo("https://test-cloudfront.net/imageUrl.png");
            assertThat(result.get(i).getSequence()).isEqualTo(changeIndex.get(i));
            assertThat(result.get(i).getUserName()).isEqualTo("userName");
        }

        verify(s3Service, times(3)).uploadImage(any(), any()); // S3에 3번 업로드
        verify(portfolioIdGenerator, times(1)).generatePortfolioId(); // Portfolio ID는 한 번만 생성
    }
}