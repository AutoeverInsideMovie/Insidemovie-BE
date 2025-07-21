package com.insidemovie.backend.api.movie.dto.boxoffice;

import com.insidemovie.backend.api.movie.entity.boxoffice.DailyBoxOfficeEntity;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;

// 일간 박스오피스 응답 DTO
@Getter
@Builder
public class DailyBoxOfficeResponseDTO {
    private Long movieId;
    private BaseBoxOfficeItemDTO base;

    public static DailyBoxOfficeResponseDTO fromEntity(DailyBoxOfficeEntity e) {
        return DailyBoxOfficeResponseDTO.builder()
            .movieId(e.getMovie() != null ? e.getMovie().getId() : null)
            .base(BaseBoxOfficeItemDTO.builder()
                .id(e.getId())
                .rnum(e.getRnum())
                .rank(e.getMovieRank())
                .rankInten(e.getRankInten())
                .rankOldAndNew(e.getRankOldAndNew())
                .movieCd(e.getMovieCd())
                .movieNm(e.getMovieName())
                .openDt(e.getOpenDate())
                .salesAmt(e.getSalesAcc())
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
