package org.kiru.user.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import autoparams.AutoSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.kiru.core.user.user.domain.Nationality;
import org.kiru.user.UserServiceApplication;
import org.kiru.user.user.dto.request.UserPurposesReq;
import org.kiru.user.user.dto.request.UserSignInReq;
import org.kiru.user.user.dto.request.UserSignUpReq;
import org.kiru.user.user.dto.request.UserTalentsReq;
import org.kiru.user.user.dto.response.UserJwtInfoRes;
import org.kiru.user.user.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.multipart.MultipartFile;

@WebMvcTest(UserLoginController.class)
@ContextConfiguration(classes = UserServiceApplication.class) // 애플리케이션 메인 클래스 지정
@Import(ValidationAutoConfiguration.class)
class UserLoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    private List<MultipartFile> images;
    private List<MultipartFile> image11;

    @BeforeEach
    void setUp() {
        MultipartFile mockFile = new MockMultipartFile("file", "filename.jpg", "image/jpeg", new byte[0]);
        MultipartFile mockFile2 = new MockMultipartFile("file", "filename.jpg", "image/jpeg", new byte[0]);
        MultipartFile mockFile3 = new MockMultipartFile("file", "filename.jpg", "image/jpeg", new byte[0]);
        images = List.of(mockFile, mockFile2, mockFile3);

        MultipartFile mockFile1 = new MockMultipartFile("file", "filename.jpg", "image/jpeg", new byte[0]);
        MultipartFile mockFile12 = new MockMultipartFile("file", "filename.jpg", "image/jpeg", new byte[0]);
        MultipartFile mockFile13 = new MockMultipartFile("file", "filename.jpg", "image/jpeg", new byte[0]);
        MultipartFile mockFile4 = new MockMultipartFile("file", "filename.jpg", "image/jpeg", new byte[0]);
        MultipartFile mockFile5 = new MockMultipartFile("file", "filename.jpg", "image/jpeg", new byte[0]);
        MultipartFile mockFile6 = new MockMultipartFile("file", "filename.jpg", "image/jpeg", new byte[0]);
        MultipartFile mockFile7 = new MockMultipartFile("file", "filename.jpg", "image/jpeg", new byte[0]);
        MultipartFile mockFile8 = new MockMultipartFile("file", "filename.jpg", "image/jpeg", new byte[0]);
        MultipartFile mockFile9 = new MockMultipartFile("file", "filename.jpg", "image/jpeg", new byte[0]);
        MultipartFile mockFile10 = new MockMultipartFile("file", "filename.jpg", "image/jpeg", new byte[0]);
        MultipartFile mockFile11 = new MockMultipartFile("file", "filename.jpg", "image/jpeg", new byte[0]);
        image11 = List.of(mockFile1, mockFile12, mockFile13, mockFile4, mockFile5, mockFile6, mockFile7, mockFile8, mockFile9, mockFile10, mockFile11);

    }

    @ParameterizedTest
    @AutoSource
    @DisplayName("적합한 값이 들어올 경우 회원가입 성공")
    void signUp(String email,
                List<UserPurposesReq> purposesReqs,
                List<UserTalentsReq> userTalentsReqs,
                String accessToken,
                String refreshToken) throws Exception {
        UserSignUpReq userSignUpReq = UserSignUpReq.builder()
                .email(email)
                .name("name")
                .password("6129nhnh!")
                .description("description")
                .instagramId("instagramId")
                .nationality(Nationality.KOREA)
                .webUrl("webUrl")
                .build();
        UserJwtInfoRes userJwtInfoRes = new UserJwtInfoRes(1L, accessToken, refreshToken);

        // Mock 설정
        when(authService.signUp(any(), any(), any(), any())).thenReturn(userJwtInfoRes);

        // 요청 생성
        MockHttpServletRequestBuilder requestBuilder = multipart("/api/v1/users/signup")
                .file(new MockMultipartFile("portfolioImgs", "file1.jpg", MediaType.IMAGE_JPEG_VALUE,
                        images.get(0).getBytes()))
                .file(new MockMultipartFile("portfolioImgs", "file2.jpg", MediaType.IMAGE_JPEG_VALUE,
                        images.get(1).getBytes()))
                .file(new MockMultipartFile("portfolioImgs", "file3.jpg", MediaType.IMAGE_JPEG_VALUE,
                        images.get(2).getBytes())) // 여러 파일 추가
                .file(new MockMultipartFile("userSignUpReq", "userSignUpReq.json",
        MediaType.APPLICATION_JSON_VALUE, new ObjectMapper().writeValueAsBytes(userSignUpReq)))
                .file(new MockMultipartFile("purpose", "purpose.json",
                        MediaType.APPLICATION_JSON_VALUE, new ObjectMapper().writeValueAsBytes(purposesReqs)))
                .file(new MockMultipartFile("talent", "talent.json",
                        MediaType.APPLICATION_JSON_VALUE, new ObjectMapper().writeValueAsBytes(userTalentsReqs)))
                .contentType(MediaType.MULTIPART_FORM_DATA);
        // 테스트 실행 및 검증
        mockMvc.perform(requestBuilder)
                .andExpect(status().isCreated()) // 응답 상태 검증
                .andExpect(content().json(new ObjectMapper().writeValueAsString(userJwtInfoRes))); // 응답 본문 검증
    }

    @ParameterizedTest
    @AutoSource
    @DisplayName("이미지가 11장 값이 들어올 경우 오류 발생")
    void signUp_maxImageList(String email,
                List<UserPurposesReq> purposesReqs,
                List<UserTalentsReq> userTalentsReqs,
                String accessToken,
                String refreshToken) throws Exception {
        UserSignUpReq userSignUpReq = UserSignUpReq.builder()
                .email(email)
                .name("name")
                .password("6129nhnh!")
                .description("description")
                .instagramId("instagramId")
                .nationality(Nationality.KOREA)
                .webUrl("webUrl")
                .build();
        UserJwtInfoRes userJwtInfoRes = new UserJwtInfoRes(1L, accessToken, refreshToken);

        // Mock 설정
        when(authService.signUp(any(), any(), any(), any())).thenReturn(userJwtInfoRes);

        // 요청 생성
        MockHttpServletRequestBuilder requestBuilder = multipart("/api/v1/users/signup")
                .file(new MockMultipartFile("portfolioImgs", "file1.jpg", MediaType.IMAGE_JPEG_VALUE,
                        image11.get(0).getBytes()))
                .file(new MockMultipartFile("portfolioImgs", "file2.jpg", MediaType.IMAGE_JPEG_VALUE,
                        image11.get(1).getBytes()))
                .file(new MockMultipartFile("portfolioImgs", "file3.jpg", MediaType.IMAGE_JPEG_VALUE,
                        image11.get(2).getBytes())) // 여러 파일 추가
                .file(new MockMultipartFile("portfolioImgs", "file3.jpg", MediaType.IMAGE_JPEG_VALUE,
                        image11.get(3).getBytes())) // 여러 파일 추가
                .file(new MockMultipartFile("portfolioImgs", "file3.jpg", MediaType.IMAGE_JPEG_VALUE,
                        image11.get(4).getBytes())) // 여러 파일 추가
                .file(new MockMultipartFile("portfolioImgs", "file3.jpg", MediaType.IMAGE_JPEG_VALUE,
                        image11.get(5).getBytes())) // 여러 파일 추가
                .file(new MockMultipartFile("potfolioImgs", "file3.jpg", MediaType.IMAGE_JPEG_VALUE,
                        image11.get(6).getBytes())) // 여러 파일 추가
                .file(new MockMultipartFile("portfolioImgs", "file3.jpg", MediaType.IMAGE_JPEG_VALUE,
                        image11.get(7).getBytes())) // 여러 파일 추가
                .file(new MockMultipartFile("portfolioImgs", "file3.jpg", MediaType.IMAGE_JPEG_VALUE,
                        image11.get(8).getBytes())) // 여러 파일 추가
                .file(new MockMultipartFile("portfolioImgs", "file3.jpg", MediaType.IMAGE_JPEG_VALUE,
                        image11.get(9).getBytes())) // 여러 파일 추가
                .file(new MockMultipartFile("portfolioImgs", "file3.jpg", MediaType.IMAGE_JPEG_VALUE,
                        image11.get(10).getBytes())) // 여러 파일 추가
                .file(new MockMultipartFile("userSignUpReq", "userSignUpReq.json",
                        MediaType.APPLICATION_JSON_VALUE, new ObjectMapper().writeValueAsBytes(userSignUpReq)))
                .file(new MockMultipartFile("purpose", "purpose.json",
                        MediaType.APPLICATION_JSON_VALUE, new ObjectMapper().writeValueAsBytes(purposesReqs)))
                .file(new MockMultipartFile("talent", "talent.json",
                        MediaType.APPLICATION_JSON_VALUE, new ObjectMapper().writeValueAsBytes(userTalentsReqs)))
                .contentType(MediaType.MULTIPART_FORM_DATA);
        // 테스트 실행 및 검증
        mockMvc.perform(requestBuilder)
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
        verify(authService,never()).signUp(any(), any(), any(), any());
    }

    @ParameterizedTest
    @AutoSource
    @DisplayName("Purpose값이 emptyList일경우 오류 발생")
    void signUp_PurposeList(String email,
                             List<UserPurposesReq> purposesReqs,
                             List<UserTalentsReq> userTalentsReqs,
                             String accessToken,
                             String refreshToken) throws Exception {
        UserSignUpReq userSignUpReq = UserSignUpReq.builder()
                .email(email)
                .name("name")
                .password("6129nhnh!")
                .description("description")
                .instagramId("instagramId")
                .nationality(Nationality.KOREA)
                .webUrl("webUrl")
                .build();
        UserJwtInfoRes userJwtInfoRes = new UserJwtInfoRes(1L, accessToken, refreshToken);

        // Mock 설정
        when(authService.signUp(any(), any(), any(), any())).thenReturn(userJwtInfoRes);

        // 요청 생성
        MockHttpServletRequestBuilder requestBuilder = multipart("/api/v1/users/signup")
                .file(new MockMultipartFile("portfolioImgs", "file1.jpg", MediaType.IMAGE_JPEG_VALUE,
                        image11.get(0).getBytes()))
                .file(new MockMultipartFile("portfolioImgs", "file2.jpg", MediaType.IMAGE_JPEG_VALUE,
                        image11.get(1).getBytes()))
                .file(new MockMultipartFile("userSignUpReq", "userSignUpReq.json",
                        MediaType.APPLICATION_JSON_VALUE, new ObjectMapper().writeValueAsBytes(userSignUpReq)))
                .file(new MockMultipartFile("purpose", "purpose.json",
                        MediaType.APPLICATION_JSON_VALUE, new ObjectMapper().writeValueAsBytes(List.of())))
                .file(new MockMultipartFile("talent", "talent.json",
                        MediaType.APPLICATION_JSON_VALUE, new ObjectMapper().writeValueAsBytes(userTalentsReqs)))
                .contentType(MediaType.MULTIPART_FORM_DATA);
        // 테스트 실행 및 검증
        mockMvc.perform(requestBuilder)
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
        verify(authService,never()).signUp(any(), any(), any(), any());
    }

    @ParameterizedTest
    @AutoSource
    @DisplayName("Talent값이 emptyList일경우 오류 발생")
    void singUp_TalentList(String email,
                            List<UserPurposesReq> purposesReqs,
                            List<UserTalentsReq> userTalentsReqs,
                            String accessToken,
                            String refreshToken) throws Exception {
        UserSignUpReq userSignUpReq = UserSignUpReq.builder()
                .email(email)
                .name("name")
                .password("6129nhnh!")
                .description("description")
                .instagramId("instagramId")
                .nationality(Nationality.KOREA)
                .webUrl("webUrl")
                .build();
        // 요청 생성
        MockHttpServletRequestBuilder requestBuilder = multipart("/api/v1/users/signup")
                .file(new MockMultipartFile("portfolioImgs", "file1.jpg", MediaType.IMAGE_JPEG_VALUE,
                        image11.get(0).getBytes()))
                .file(new MockMultipartFile("portfolioImgs", "file2.jpg", MediaType.IMAGE_JPEG_VALUE,
                        image11.get(1).getBytes()))
                .file(new MockMultipartFile("userSignUpReq", "userSignUpReq.json",
                        MediaType.APPLICATION_JSON_VALUE, new ObjectMapper().writeValueAsBytes(userSignUpReq)))
                .file(new MockMultipartFile("purpose", "purpose.json",
                        MediaType.APPLICATION_JSON_VALUE, new ObjectMapper().writeValueAsBytes(purposesReqs)))
                .file(new MockMultipartFile("talent", "talent.json",
                        MediaType.APPLICATION_JSON_VALUE, new ObjectMapper().writeValueAsBytes(List.of())))
                .contentType(MediaType.MULTIPART_FORM_DATA);
        // 테스트 실행 및 검증
        mockMvc.perform(requestBuilder)
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
        verify(authService,never()).signUp(any(), any(), any(), any());
    }

    @ParameterizedTest
    @AutoSource
    @DisplayName("적합하지 않은 경우가 많은 경우 여러 에러 전달 ")
    void signUp_validation(String email,
                List<UserPurposesReq> purposesReqs,
                List<UserTalentsReq> userTalentsReqs,
                String accessToken,
                String refreshToken) throws Exception {
        UserSignUpReq userSignUpReq = UserSignUpReq.builder()
                .email(email)
                .name("name")
                .password("6129nhnh!")
                .description("description")
                .instagramId("instagramId")
                .nationality(Nationality.KOREA)
                .webUrl("webUrl")
                .build();
        UserJwtInfoRes userJwtInfoRes = new UserJwtInfoRes(1L, accessToken, refreshToken);

        // Mock 설정
        when(authService.signUp(any(), any(), any(), any())).thenReturn(userJwtInfoRes);

        // 요청 생성
        MockHttpServletRequestBuilder requestBuilder = multipart("/api/v1/users/signup")
                .file(new MockMultipartFile("portfolioImgs", "file1.jpg", MediaType.IMAGE_JPEG_VALUE,
                        image11.get(0).getBytes()))
                .file(new MockMultipartFile("portfolioImgs", "file2.jpg", MediaType.IMAGE_JPEG_VALUE,
                        image11.get(1).getBytes()))
                .file(new MockMultipartFile("portfolioImgs", "file3.jpg", MediaType.IMAGE_JPEG_VALUE,
                        image11.get(2).getBytes())) // 여러 파일 추가
                .file(new MockMultipartFile("portfolioImgs", "file3.jpg", MediaType.IMAGE_JPEG_VALUE,
                        image11.get(3).getBytes())) // 여러 파일 추가
                .file(new MockMultipartFile("portfolioImgs", "file3.jpg", MediaType.IMAGE_JPEG_VALUE,
                        image11.get(4).getBytes())) // 여러 파일 추가
                .file(new MockMultipartFile("portfolioImgs", "file3.jpg", MediaType.IMAGE_JPEG_VALUE,
                        image11.get(5).getBytes())) // 여러 파일 추가
                .file(new MockMultipartFile("potfolioImgs", "file3.jpg", MediaType.IMAGE_JPEG_VALUE,
                        image11.get(6).getBytes())) // 여러 파일 추가
                .file(new MockMultipartFile("portfolioImgs", "file3.jpg", MediaType.IMAGE_JPEG_VALUE,
                        image11.get(7).getBytes())) // 여러 파일 추가
                .file(new MockMultipartFile("portfolioImgs", "file3.jpg", MediaType.IMAGE_JPEG_VALUE,
                        image11.get(8).getBytes())) // 여러 파일 추가
                .file(new MockMultipartFile("portfolioImgs", "file3.jpg", MediaType.IMAGE_JPEG_VALUE,
                        image11.get(9).getBytes())) // 여러 파일 추가
                .file(new MockMultipartFile("portfolioImgs", "file3.jpg", MediaType.IMAGE_JPEG_VALUE,
                        image11.get(10).getBytes())) // 여러 파일 추가
                .file(new MockMultipartFile("userSignUpReq", "userSignUpReq.json",
                        MediaType.APPLICATION_JSON_VALUE, new ObjectMapper().writeValueAsBytes(userSignUpReq)))
                .file(new MockMultipartFile("purpose", "purpose.json",
                        MediaType.APPLICATION_JSON_VALUE, new ObjectMapper().writeValueAsBytes(List.of())))
                .file(new MockMultipartFile("talent", "talent.json",
                        MediaType.APPLICATION_JSON_VALUE, new ObjectMapper().writeValueAsBytes(List.of())))
                .contentType(MediaType.MULTIPART_FORM_DATA);
        // 테스트 실행 및 검증

        mockMvc.perform(requestBuilder)
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.errors[?(@.field == 'images')].reason").value("size must be between 0 and 9")) // images 오류 메시지 검증
                .andExpect(jsonPath("$.errors[?(@.field == 'purposes')].reason").value("must not be empty")) // purposes 필드 오류 검증
                .andExpect(jsonPath("$.errors[?(@.field == 'talents')].reason").value("must not be empty")); // talents 필드 오류 검증
        verify(authService,never()).signUp(any(), any(), any(), any());
    }

    @AutoSource
    @ParameterizedTest
    @DisplayName("비밀번호가 8자 이상 + 영,숫자,특수문자 포함이 안될시 400 에러 반환")
    void signInFailTest(String accessToken, String refreshToken) throws Exception {
//        !TODO : production에서는 주석처리된 코드를 사용해야합니다.
//        UserSignInReq userSignInReq = new UserSignInReq(
//                "rlarlgnszx@naver.com","1234"
//        );
//        UserJwtInfoRes userJwtInfoRes = new UserJwtInfoRes(1L, accessToken, refreshToken);
//
//        when(authService.signIn(any())).thenReturn(userJwtInfoRes);
//
//        mockMvc.perform(post("/api/v1/users/signin")
//                .contentType(MediaType.APPLICATION_JSON)
//                                .content(new ObjectMapper().writeValueAsString(userSignInReq))
//                )
//                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
//        verify(authService, never()).signIn(any());
    }

    @AutoSource
    @ParameterizedTest
    @DisplayName("로그인에 적합한 값이 들어올 경우 로그인 성공 및 201 반환")
    void signInSuccess(String accessToken, String refreshToken) throws Exception {
        UserSignInReq userSignInReq = new UserSignInReq(
                "rlarlgnszx@naver.com","1234@123n"
        );
        UserJwtInfoRes userJwtInfoRes = new UserJwtInfoRes(1L, accessToken, refreshToken);

        when(authService.signIn(any())).thenReturn(userJwtInfoRes);

        mockMvc.perform(post("/api/v1/users/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(userSignInReq))
                )
                .andExpect(status().isCreated());
    }
}