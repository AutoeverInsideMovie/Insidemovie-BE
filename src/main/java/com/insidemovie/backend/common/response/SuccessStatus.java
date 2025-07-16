package com.insidemovie.backend.common.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum SuccessStatus {
    /** 200 SUCCESS */
    SEND_REGISTER_SUCCESS(HttpStatus.OK,"회원가입 성공"),
    SEND_LOGIN_SUCCESS(HttpStatus.OK, "로그인 성공"),
    SEND_TOKEN_REISSUE_SUCCESS(HttpStatus.OK, "토큰 재발급 성공"),
    SEND_KAKAO_LOGIN_SUCCESS(HttpStatus.OK, "카카오 로그인 성공"),
    SEND_KAKAO_ACCESS_TOKEN_SUCCESS(HttpStatus.OK, "카카오 액세스 토큰 발급 성공"),
    SEND_REVIEW_SUCCESS(HttpStatus.OK,"리뷰 목록 조회 성공"),
    SEND_DAILY_BOXOFFICE_SUCCESS(HttpStatus.OK, "일간 박스오피스 순위 조회 성공"),
    SEND_WEEKLY_BOXOFFICE_SUCCESS(HttpStatus.OK, "주간 박스오피스 순위 조회 성공"),
    MODIFY_REVIEW_SUCCESS(HttpStatus.OK,"리뷰 수정 성공"),
    DELETE_REVIEW_SUCCESS(HttpStatus.OK,"리뷰 삭제 성공"),
    UPDATE_NICKNAME_SUCCESS(HttpStatus.OK, "닉네임 수정 성공"),
    UPDATE_EMOTION_SUCCESS(HttpStatus.OK, "메인 감정 수정 성공"),
    UPDATE_PASSWORD_SUCCESS(HttpStatus.OK, "비밀번호 수정 성공"),
    SEND_REVIEW_LIKE_SUCCESS(HttpStatus.OK, "리뷰 좋아요 토글 성공"),
    SEND_MY_REVIEW_SUCCESS(HttpStatus.OK, "내 리뷰 목록 조회 성공"),
    REPORT_CREATE_SUCCESS(HttpStatus.OK, "신고 접수 성공"),
    SEND_MEMBER_LIST_SUCCESS(HttpStatus.OK, "사용자 목록 조회 성공"),
    MEMBER_BAN_SUCCESS(HttpStatus.OK, "회원 정지 성공"),
    MEMBER_UNBAN_SUCCESS(HttpStatus.OK, "회원 정지 해제 성공"),
    LOGOUT_SUCCESS(HttpStatus.OK, "회원 로그아웃 성공"),

    /** 201 CREATED */
    CREATE_SAMPLE_SUCCESS(HttpStatus.CREATED, "샘플 등록 성공"),
    CREATE_REVIEW_SUCCESS(HttpStatus.CREATED, "리뷰 등록 성공"),
    ;

    private final HttpStatus httpStatus;
    private final String message;

    public int getStatusCode() {
        return this.httpStatus.value();
    }
}
