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
    SEND_KAKAO_REGISTER_SUCCESS(HttpStatus.OK, "카카오 회원가입 성공"),
    SEND_KAKAO_ACCESS_TOKEN_SUCCESS(HttpStatus.OK, "카카오 액세스 토큰 발급 성공"),
    SEND_REVIEW_SUCCESS(HttpStatus.OK,"리뷰 목록 조회 성공"),
    MODIFY_REVIEW_SUCCESS(HttpStatus.OK,"리뷰 수정 성공"),
    DELETE_REVIEW_SUCCESS(HttpStatus.OK,"리뷰 삭제 성공"),
    UPDATE_NICKNAME_SUCCESS(HttpStatus.OK, "닉네임 수정 성공"),
    UPDATE_EMOTION_SUCCESS(HttpStatus.OK, "메인 감정 수정 성공"),
    UPDATE_PASSWORD_SUCCESS(HttpStatus.OK, "비밀번호 수정 성공"),
    SEND_REVIEW_LIKE_SUCCESS(HttpStatus.OK, "리뷰 좋아요 토글 성공"),
    SEND_MY_REVIEW_SUCCESS(HttpStatus.OK, "내 리뷰 조회 성공"),
    SEND_MY_MOVIE_SUCCESS(HttpStatus.OK, "내 영화 좋아요 목록 조회 성공"),
    REPORT_CREATE_SUCCESS(HttpStatus.OK, "신고 접수 성공"),
    SEND_MEMBER_LIST_SUCCESS(HttpStatus.OK, "사용자 목록 조회 성공"),
    SEND_MEMBER_SUCCESS(HttpStatus.OK, "사용자 조회 성공"),
    MEMBER_BAN_SUCCESS(HttpStatus.OK, "회원 정지 성공"),
    MEMBER_UNBAN_SUCCESS(HttpStatus.OK, "회원 정지 해제 성공"),
    SEND_MOVIE_DETAIL_SUCCESS(HttpStatus.OK, "영화 상세 조회 성공"),
    SEND_MOVIE_LIKE_SUCCESS(HttpStatus.OK, "영화 좋아요 토글 성공"),
    SEND_REPORT_LIST_SUCCESS(HttpStatus.OK, "신고 목록 조회 성공"),
    REPORT_ACCEPTED(HttpStatus.OK, "신고 수용 성공"),
    REPORT_REJECTED(HttpStatus.OK, "신고 기각 성공"),
    REPORT_UNPROCESSED(HttpStatus.OK, "신고 보류 성공"),
    SEND_DASHBOARD_SUCCESS(HttpStatus.OK, "대시보드 조회 성공"),
    SEND_DAILY_BOXOFFICE_SUCCESS(HttpStatus.OK, "일간 박스오피스 조회 성공"),
    SEND_WEEKLY_BOXOFFICE_SUCCESS(HttpStatus.OK, "주간 박스오피스 조회 성공"),
    LOGOUT_SUCCESS(HttpStatus.OK, "로그아웃 성공."),
    SEND_EMOTION_SUMMARY_SUCCESS(HttpStatus.OK, "감정 평균 조회 성공"),
    SEARCH_MOVIES_SUCCESS(HttpStatus.OK, "영화 검색 성공"),
    SEND_MOVIE_EMOTION_SUCCESS(HttpStatus.OK, "영화 감정 상태 조회 성공"),
    SEND_INITIAL_EMOTION_SUMMARY_SUCCESS(HttpStatus.OK, "초기 감정 상태 등록 완료"),
    UPDATE_EMOTION_SUMMARY_SUCCESS(HttpStatus.OK, "사용자 감정 상태 업데이트 완료"),
    UPDATE_PROFILE_IMAGE_SUCCESS(HttpStatus.OK, "프로필 이미지 변경 성공"),
    CHECK_NICKNAME_SUCCESS(HttpStatus.OK, "닉네임 중복 여부 확인 성공"),
    SEND_POPULAR_MOVIES_SUCCESS(HttpStatus.OK, "인기 영화 조회 성공"),
    SEND_BOXOFFICE_MOVIE_DETAIL_SUCCESS(HttpStatus.OK, "박스오피스의 상세정보 조회 성공"),
    SEND_GENRE_MOVIES_SUCCESS(HttpStatus.OK, "장르별 영화 조회 성공"),
    SEND_WATCHED_MOVIES_SUCCESS(HttpStatus.OK, "내가 관람한 영화 조회 성공"),
    SEND_RECOMMEND_MOVIES_SUCCESS(HttpStatus.OK, "추천 영화 조회 성공"),
    SEND_VOTE_SUCCESS(HttpStatus.OK, "영화 투표 성공"),
    GET_MATCH_DETAIL_SUCCESS(HttpStatus.OK, "대결 영화 조회 성공"),
    GET_WINNER_SUCCESS(HttpStatus.OK, "우승 내역 조회 성공"),
    SEND_EMAIL_SUCCESS(HttpStatus.OK, "이메일 인증코드 전송 성공"),
    VERIFY_CODE_SUCCESS(HttpStatus.OK, "인증번호 검증 성공"),


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
