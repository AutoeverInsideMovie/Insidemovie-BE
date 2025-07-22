package com.insidemovie.backend.api.movie.dto.boxoffice;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

// 일간/주간 공통 박스오피스 리스트 래퍼
@Getter
@Builder
public class BoxOfficeListDTO<T> {
    private String boxofficeType;
    private String targetDt;
    private List<T> items; // DailyBoxOfficeResponseDTO 또는 WeeklyBoxOfficeResponseDTO
    private BigDecimal ratingAvg;
}
