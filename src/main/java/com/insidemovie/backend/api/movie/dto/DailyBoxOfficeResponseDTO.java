package com.insidemovie.backend.api.movie.dto;

import com.insidemovie.backend.api.movie.entity.DailyBoxOfficeEntity;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;

// 일간 박스오피스 응답 DTO
@Getter
@Builder
public class DailyBoxOfficeResponseDTO {
    private BaseBoxOfficeItemDTO base;
    private LocalDate targetDate;
    private String boxofficeType;
    private String showRange;

    public static DailyBoxOfficeResponseDTO fromEntity(DailyBoxOfficeEntity e) {
        return DailyBoxOfficeResponseDTO.builder()
            .base(BaseBoxOfficeItemDTO.builder()
                .id(e.getId())
                .rnum(e.getRnum())
                .rank(e.getRank())
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
            .targetDate(e.getTargetDate())
            .boxofficeType("일별")
            .showRange(e.getTargetDate().toString() + "~" + e.getTargetDate().toString())
            .build();
    }
}
