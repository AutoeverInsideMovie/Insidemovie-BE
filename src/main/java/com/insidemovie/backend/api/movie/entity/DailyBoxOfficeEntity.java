package com.insidemovie.backend.api.movie.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;


@Entity
@Table(name = "daily_box_office")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyBoxOfficeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate targetDate;       // 조회 일자
    private String rnum;                // 순번
    private String movieRank;                // 해당 일자 박스오피스 순위
    private String rankInten;           // 전일 대비 순위 증감분
    private String rankOldAndNew;       // 랭킹 신규 진입 여부(OLD or NEW)
    private String movieCd;             // 영화 대표 코드
    private String movieName;           // 영화 제목(국문)
    private String openDate;            // 개봉일
    private String salesShare;          // 매출 총액 대비 매출 비율
    private String salesInten;          // 전일 대비 매출액 증감분
    private String salesChange;         // 전일 대비 매출액 증감 비율
    private String salesAcc;            // 누적 매출액
    private String audiCnt;             // 해당일 관객수
    private String audiInten;           // 전일 대비 관객수 증감분
    private String audiChange;          // 전일 대비 관객수 증감 비율
    private String audiAcc;             // 누적 관객수
    private String scrnCnt;             // 해당일 상영한 스크린 수
    private String showCnt;             // 해당일 상영된 횟수
}
