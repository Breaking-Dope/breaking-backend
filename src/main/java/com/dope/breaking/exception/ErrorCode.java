package com.dope.breaking.exception;

/**
 * 에러 코드는, 최대한 Exception 클래스의 이름과 동일하게 맞춰주세요.
 * ex) InvalidAccessTokenException.class -> ErrorCode.INVALID_ACCESS_TOKEN
 *
 * 해당 에러 코드와 라벨은, Notion 문서의 (코드명, 메시지) 와 일치시킵니다.
 */
public enum ErrorCode {

    REQUIRE_LOGIN("BSE000", "로그인이 필요합니다."),
    INVALID_ACCESS_TOKEN("BSE001", "Access Token이 유효하지 않습니다."),
    EXPIRED_ACCESS_TOKEN("BSE002", "Access Token이 만료되었습니다."),
    INVALID_REFRESH_TOKEN("BSE003", "Refresh Token이 유효하지 않습니다."),
    EXPIRED_REFRESH_TOKEN("BSE004", "Refresh Token이 만료되었습니다."),

    BAD_REQUEST("BSE400", "클라이언트 요청오류."),
    NO_SUCH_USER("BSE401", "해당 유저를 찾을 수 없니다."),
    INVALID_NICKNAME_FORMAT("BSE410", "닉네임 형식이 잘못되었습니다."),
    INVALID_PHONE_NUMBER_FORMAT("BSE411", "전화번호 형식이 잘못되었습니다."),
    INVALID_EMAIL_FORMAT("BSE412", "이메일 형식이 잘못되었습니다."),
    INVALID_USER_ROLE("BSE412", "유저 형식이 잘못되었습니다."),
    DUPLICATED_NICKNAME("BSE413", "사용중인 닉네임입니다."),
    DUPLICATED_PHONE_NUMBER("BSE414", "사용중인 전화번호입니다."),
    DUPLICATED_EMAIL("BSE415", "사용중인 이메일입니다."),
    ALREADY_FOLLOWING("BSE420","이미 팔로우 중인 유저입니다."),
    ALREADY_UNFOLLOWING("BSE421","이미 언팔로우 중인 유저입니다."),

    INTERNAL_SERVER_ERROR("BSE500", "서버 요청 처리 실패.")
    ;

    private String code;
    private String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    };

}
