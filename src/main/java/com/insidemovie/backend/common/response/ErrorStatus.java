package com.insidemovie.backend.common.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum ErrorStatus {
    /** 400 BAD_REQUEST */
    VALIDATION_REQUEST_MISSING_EXCEPTION(HttpStatus.BAD_REQUEST, "요청 값이 입력되지 않았습니다."),
    ALREADY_EMAIL_EXIST_EXCEPTION(HttpStatus.BAD_REQUEST, "이미 가입된 이메일입니다."),
    PASSWORD_MISMATCH_EXCEPTION(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    KAKAO_LOGIN_FAILED(HttpStatus.BAD_REQUEST, "카카오 로그인에 실패했습니다."),
    KAKAO_TOKEN_REQUEST_FAILED(HttpStatus.BAD_REQUEST, "카카오 토큰 요청에 실패했습니다."),
    INTERNAL_TOKEN_PARSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "카카오 토큰 응답 파싱 실패"),
    KAKAO_USERINFO_REQUEST_FAILED(HttpStatus.BAD_REQUEST, "카카오 사용자 정보 요청에 실패했습니다."),
    INTERNAL_USERINFO_PARSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "카카오 사용자 정보 파싱에 실패했습니다."),
    DUPLICATE_REVIEW_EXCEPTION(HttpStatus.BAD_REQUEST, "이미 해당 영화에 작성한 리뷰가 존재합니다."),
    DUPLICATE_REPORT_EXCEPTION(HttpStatus.BAD_REQUEST, "이미 신고한 리뷰입니다."),

    /** 401 UNAUTHORIZED */
    USER_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."),


    /** 404 NOT_FOUND */
    NOT_FOUND_MEMBERID_EXCEPTION(HttpStatus.NOT_FOUND, "존재하지 않는 사용자 입니다."),
    NOT_FOUND_MOVIE_EXCEPTION(HttpStatus.NOT_FOUND, "존재하지 않는 영화 입니다."),
    NOT_FOUND_REVIEW_EXCEPTION(HttpStatus.NOT_FOUND, "존재하지 않는 리뷰 입니다."),
    NOT_FOUND_REPORT_EXCEPTION(HttpStatus.NOT_FOUND, "존재하지 않는 신고입니다."),
    NOT_FOUND_DAILY_BOXOFFICE(HttpStatus.NOT_FOUND, "일간 박스오피스 데이터를 찾을 수 없습니다."),
    NOT_FOUND_WEEKLY_BOXOFFICE(HttpStatus.NOT_FOUND, "주간 박스오피스 데이터를 찾을 수 없습니다."),

    /** 500 SERVER_ERROR */
    FAIL_UPLOAD_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR,"파일 업로드 실패하였습니다."),

    /** 503 ERROR */
    EXTERNAL_SERVICE_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "외부 감정 분석 서비스 호출에 실패하였습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String message;

    public int getStatusCode() {
        return this.httpStatus.value();
    }
}
