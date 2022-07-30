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

    NO_PERMISSION("BSE005", "해당 기능에 대한 권한이 없습니다."),
    BAD_REQUEST("BSE400", "클라이언트 요청오류."),
    NO_SUCH_USER("BSE401", "해당 유저를 찾을 수 없습니다."),

    INVALID_NICKNAME_FORMAT("BSE410", "닉네임 형식이 잘못되었습니다."),
    INVALID_PHONE_NUMBER_FORMAT("BSE411", "전화번호 형식이 잘못되었습니다."),
    INVALID_EMAIL_FORMAT("BSE412", "이메일 형식이 잘못되었습니다."),
    INVALID_USER_ROLE("BSE412", "유저 형식이 잘못되었습니다."),
    DUPLICATED_NICKNAME("BSE413", "사용중인 닉네임입니다."),
    DUPLICATED_PHONE_NUMBER("BSE414", "사용중인 전화번호입니다."),
    DUPLICATED_EMAIL("BSE415", "사용중인 이메일입니다."),
    ALREADY_FOLLOWING("BSE420","이미 팔로우 중인 유저입니다."),
    ALREADY_UNFOLLOWING("BSE421","이미 언팔로우 중인 유저입니다."),
    NO_SUCH_POST("BSE450","해당 제보를 찾을 수 없습니다"),
    NO_SUCH_COMMENT("BSE451","해당 댓글을 찾을 수 없습니다"),
    PURCHASED_POST("BSE452", "판매된 게시글은 삭제할 수 없습니다."),

    ALREADY_BOOKMARKED("BSE456", "이미 북마크를 선택했습니다."),

    ALREADY_UNBOOKMARKED("BSE457", "이미 북마크를 선택하지 않았습니다."),
    ALREADY_LIKED("BSE458","이미 좋아요를 선택했습니다."),
    ALREADY_UNLIKED("BSE459","이미 좋아요를 선택하지 않았습니다."),
    NOT_PURCHASABLE_POST("BSE460","구매 제한이 된 포스트입니다."),
    SOLD_EXCLUSIVE_POST("BSE461","이미 판매 된 단독제보입니다."),

    INTERNAL_SERVER_ERROR("BSE500", "서버 요청 처리 실패."),

    NOT_ENOUGH_BALANCE("BSE601", "금액이 부족합니다.")

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
