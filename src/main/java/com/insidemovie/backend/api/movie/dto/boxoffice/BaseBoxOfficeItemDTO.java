package com.insidemovie.backend.api.movie.dto.boxoffice;

import lombok.Builder;
import lombok.Getter;

// 박스오피스 항목의 공통 속성

@Getter
@Builder
public class BaseBoxOfficeItemDTO {
    private Long id;
    private String rnum;
    private String rank;
    private String rankInten;
    private String rankOldAndNew;
    private String movieCd;
    private String movieNm;
    private String openDt;
    private String salesAmt;
    private String salesShare;
    private String salesInten;
    private String salesChange;
    private String salesAcc;
    private String audiCnt;
    private String audiInten;
    private String audiChange;
    private String audiAcc;
    private String scrnCnt;
    private String showCnt;
}

