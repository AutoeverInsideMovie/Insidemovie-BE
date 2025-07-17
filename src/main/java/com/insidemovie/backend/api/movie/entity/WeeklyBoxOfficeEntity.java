package com.insidemovie.backend.api.movie.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "weekly_box_office")
public class WeeklyBoxOfficeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "year_week_time")
    private String yearWeekTime;    // 연도+주차 (YYYYIWww)
    private String rnum;            // 순번
    private String movieRank;            // 순위
    private String rankInten;       // 순위 증감
    private String rankOldAndNew;   // 신규 진입 여부
    private String movieCd;         // 영화 코드
    private String movieNm;         // 영화 이름
    private String openDt;          // 개봉일
    private String salesAmt;        // 매출액
    private String salesShare;      // 매출 비율
    private String salesInten;      // 매출 증감분
    private String salesChange;     // 매출 증감 비율
    private String salesAcc;        // 누적 매출액
    private String audiCnt;         // 관객 수
    private String audiInten;       // 관객 증감분
    private String audiChange;      // 관객 증감 비율
    private String audiAcc;         // 누적 관객수
    private String scrnCnt;         // 상영 스크린 수
    private String showCnt;         // 상영 횟수
}
