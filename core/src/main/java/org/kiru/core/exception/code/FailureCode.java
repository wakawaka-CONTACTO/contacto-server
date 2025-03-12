package org.kiru.core.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FailureCode {
    /**
     * 400 Bad Request
     */
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "e4000", "잘못된 요청입니다."),
    INVALID_PLATFORM_TYPE(HttpStatus.BAD_REQUEST, "e4001", "잘못된 플랫폼 타입입니다."),
    INVALID_USER_CREDENTIALS(HttpStatus.BAD_REQUEST, "e4002", "잘못된 로그인 정보입니다."),
    INVALID_IMAGE_TYPE(HttpStatus.BAD_REQUEST, "e4003", "지원하지 않는 이미지 확장자 입니다."),
    INVALID_IMAGE_SIZE(HttpStatus.BAD_REQUEST, "e4004", "지원하지 않는 이미지 크기 입니다."),
    WRONG_IMAGE_URL(HttpStatus.BAD_REQUEST, "e4005", "잘못된 이미지 URL 입니다."),
    INVALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST, "e4006", "비밀번호 형식이 올바르지 않습니다."),
    INVALID_NICKNAME_FORMAT(HttpStatus.BAD_REQUEST, "e4007", "닉네임 형식이 올바르지 않습니다."),
    INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, "e4008", "이메일 형식이 올바르지 않습니다."),
    WRONG_IMAGE_LIST_SIZE(HttpStatus.BAD_REQUEST, "e4009", "데이트 코스 이미지는 최대 10장입니다.."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "e4102", "필드가 잘못되었습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "e4103", "잘못된 필드를 넣었습니다."),
    INVALID_DISCORD_SIGNUP_MESSAGE(HttpStatus.BAD_REQUEST, "e4104", "회원가입 디스코드 알림 전송에 실패하였습니다."),
    INVALID_IMAGE_EDIT(HttpStatus.BAD_REQUEST, "e4105", "프로필 이미지 수정에 실패하였습니다."),
    SOCKET_CONNECTED_FAILED(HttpStatus.BAD_REQUEST, "e4106", "소켓 연결에 실패하였습니다."),
    NATIONALITY_NOT_PROVIDED(HttpStatus.BAD_REQUEST, "e4001", "국적 정보가 제공되지 않았습니다. 올바른 국적을 입력해 주세요."),
    /**
     * 401 Unauthorized
     */
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "e4010", "리소스 접근 권한이 없습니다."),
    INVALID_ACCESS_TOKEN_VALUE(HttpStatus.UNAUTHORIZED, "e4011", "액세스 토큰의 값이 올바르지 않습니다."),
    EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "e4012", "액세스 토큰이 만료되었습니다. 재발급 받아주세요."),
    TOKEN_SUBJECT_NOT_NUMERIC_STRING(HttpStatus.UNAUTHORIZED, "e4013", "토큰의 subject가 숫자 문자열이 아닙니다."),
    UNSUPPORTED_TOKEN_TYPE(HttpStatus.UNAUTHORIZED, "e4014", "잘못된 토큰 형식입니다."),
    MALFORMED_TOKEN(HttpStatus.UNAUTHORIZED, "e4015", "잘못된 토큰 구조입니다."),
    INVALID_SIGNATURE_TOKEN(HttpStatus.UNAUTHORIZED, "e4016", "잘못된 토큰 서명입니다."),
    KAKAO_INTERNER_ERROR(HttpStatus.UNAUTHORIZED, "e4017", "카카오 내부 서버 에러입니다."),
    INVALID_KAKAO_TOKEN(HttpStatus.UNAUTHORIZED, "e4018", "잘못된 카카오 액세스 토큰 형식입니다"),
    INVALID_REFRESH_TOKEN_VALUE(HttpStatus.UNAUTHORIZED, "e40114", "잘못된 리프레시토큰입니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "e40115", "리프레시 토큰 기간이 만료되었습니다. 재로그인 해주세요"),
    INVALID_KAKAO_ACCESS(HttpStatus.UNAUTHORIZED, "e40116", "잘못된 카카오 통신 접근입니다."),
    UN_LINK_WITH_KAKAO_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "e40117", "카카오 연결 끊기 통신에 실패했습니다"),
    INVALID_APPLE_TOKEN_ACCESS(HttpStatus.UNAUTHORIZED, "e40118", "잘못된 애플 토큰 통신 접근입니다."),
    INVALID_DATE_GET_TYPE(HttpStatus.UNAUTHORIZED, "e40119", "잘못된 데이트 타입 검색입니다."),
    INVALID_TRANSACTION_TYPE(HttpStatus.UNAUTHORIZED, "e40120", "잘못된 포인트 거래 타입 검색입니다."),
    INVALID_REGION_TYPE(HttpStatus.UNAUTHORIZED, "e40121", "잘못된 지역 입력입니다."),
    INVALID_TOKEN_USER(HttpStatus.UNAUTHORIZED, "e4019", "토큰의 유저 정보가 올바르지 않습니다."),
    PASSWORD_MISMATCH(HttpStatus.UNAUTHORIZED, "e4020", "비밀번호가 일치하지 않습니다."),
    EMAIL_LOGIN_REQUIRED(HttpStatus.UNAUTHORIZED, "e4022", "이메일 로그인이 필요한 계정입니다."),
    INVALID_PASSWORD(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS,"e4023" ,"userPassword 값이 잘못되었습니다." ),
    /**
     * 403 Forbidden
     */
    FORBIDDEN(HttpStatus.FORBIDDEN, "e4030", "리소스 접근 권한이 없습니다."),
    CHAT_ROOM_JOIN_FAILED(HttpStatus.FORBIDDEN, "e4031", "채팅방 참여에 실패했습니다."),
    CHAT_ROOM_ACCESS_DENIED(HttpStatus.FORBIDDEN, "e4032", "채팅방에 접근 권한이 없습니다."),
    CHAT_MESSAGE_SEND_FAILED(HttpStatus.FORBIDDEN, "e4033", "메시지 전송에 실패했습니다."),
    USER_PROFILE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "e4034", "프로필 접근 권한이 없습니다."),
    USER_UPDATE_FAILED(HttpStatus.FORBIDDEN, "e4035", "프로필 수정 권한이 없습니다."),
    PORTFOLIO_ACCESS_DENIED(HttpStatus.FORBIDDEN, "e4036", "포트폴리오 접근 권한이 없습니다."),

    /**
     * 404 Not Found
     */
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "e4040", "대상을 찾을 수 없습니다."),
    TOKEN_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "e4041", "찾을 수 없는 토큰 타입입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "e4042", "유저를 찾을 수 없습니다."),
    CHATROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "e4043", "채팅방을 찾을 수 없습니다."),
    PORTFOLIO_NOT_FOUND(HttpStatus.NOT_FOUND, "e4044", "포트폴리오를 찾을 수 없습니다."),
    USER_LIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "e4045", "좋아요 정보를 찾을 수 없습니다."),
    USER_PURPOSE_NOT_FOUND(HttpStatus.NOT_FOUND, "e4046", "유저 목적을 찾을 수 없습니다."),
    USER_TALENT_NOT_FOUND(HttpStatus.NOT_FOUND, "e4047", "유저 재능을 찾을 수 없습니다."),
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "e4048", "이미지를 찾을 수 없습니다."),

    /**
     * 405 Method Not Allowed
     */
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "e4050", "잘못된 HTTP method 요청입니다."),

    /**
     * 409 Conflict
     */
    CONFLICT(HttpStatus.CONFLICT, "e4090", "이미 존재하는 리소스입니다."),
    DUPLICATE_USER(HttpStatus.CONFLICT, "e4091", "이미 존재하는 유저입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "e4092", "이미 존재하는 닉네임입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "e4093", "이미 존재하는 이메일입니다."),

    /**
     * 500 Internal Server Error
     */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "e5000", "서버 내부 오류입니다."),
    COURSE_CREATE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"e5001" , "코스 생성에 실패했습니다."),
    POINT_CREATE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "e5002", "포인트 생성에 실패했습니다."),
    REDIS_CONNECTION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "e5003", "Redis 연결에 실패했습니다."),
    DISCORD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "e5004", "디스코드 로그 전송 내용이 존재하지 않습니다."), RESOURCE_NOT_FOUND(
            HttpStatus.NOT_FOUND,"e4404" , "요청한 리소스를 찾을수 없습니다." ),
    INVALID_USER_LIKE(HttpStatus.BAD_REQUEST,"e402" ,"좋아요하는 userID가 같습니다." ),
    SERVICE_UNAVAILABLE(HttpStatus.INTERNAL_SERVER_ERROR, "e5005", "서비스가 이용 불가능합니다."),
    INVALID_CONFIGURATION(HttpStatus.INTERNAL_SERVER_ERROR, "e5006", "yaml 설정이 로드되지 않았습니다"),
    PORTFOLIO_ID_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "e5007", "포트폴리오 ID 생성에 실패했습니다.");


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
