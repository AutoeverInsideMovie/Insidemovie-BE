package com.insidemovie.backend.api.movie.dto.boxoffice;

import com.insidemovie.backend.api.movie.entity.boxoffice.WeeklyBoxOfficeEntity;
import lombok.Builder;
import lombok.Getter;

// 주간 박스오피스 응답 DTO
@Getter
@Builder
public class WeeklyBoxOfficeResponseDTO {
    private BaseBoxOfficeItemDTO base;
    private Long movieId;

    public static WeeklyBoxOfficeResponseDTO fromEntity(WeeklyBoxOfficeEntity e) {
        return WeeklyBoxOfficeResponseDTO.builder()
            .movieId(e.getMovie() != null ? e.getMovie().getId() : null)
            .base(BaseBoxOfficeItemDTO.builder()
                .id(e.getId())
                .rnum(e.getRnum())
                .rank(e.getMovieRank())
                .rankInten(e.getRankInten())
                .rankOldAndNew(e.getRankOldAndNew())
                .movieCd(e.getMovieCd())
                .movieNm(e.getMovieNm())
                .openDt(e.getOpenDt())
                .salesAmt(e.getSalesAmt())
                .salesShare(e.getSalesShare())
                .salesInten(e.getSalesInten())
                .salesChange(e.getSalesChange())
                .salesAcc(e.getSalesAcc())
                .audiCnt(e.getAudiCnt())
                .audiInten(e.getAudiInten())
                .audiChange(e.getAudiChange())
                .audiAcc(e.getAudiAcc())
                .scrnCnt(e.getScrnCnt())
                .showCnt(e.getShowCnt())
                .build())
            .build();
    }
}
