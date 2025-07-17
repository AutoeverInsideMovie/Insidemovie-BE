package com.insidemovie.backend.api.movie.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BoxOfficeRequestDTO {
    private String targetDt;         // yyyyMMdd
    private Integer itemPerPage;     // default 10
    private String multiMovieYn;     // Y/N
    private String repNationCd;      // K/F
    private String wideAreaCd;       // 지역 코드
    private String weekGb;           // 주간 조회 시 설정 ("0" 등), 일간이면 null
}
