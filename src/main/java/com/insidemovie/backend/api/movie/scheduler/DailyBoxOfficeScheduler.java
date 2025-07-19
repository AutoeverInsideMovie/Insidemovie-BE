package com.insidemovie.backend.api.movie.scheduler;

import com.insidemovie.backend.api.movie.dto.boxoffice.BoxOfficeRequestDTO;
import com.insidemovie.backend.api.movie.service.BoxOfficeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
@Slf4j
public class DailyBoxOfficeScheduler {

    private final BoxOfficeService boxOfficeService;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Scheduled(cron = "${scheduler.cron.daily}", zone = "${scheduler.zone}")
    @Transactional
    public void fetchAndStoreDailyBoxOffice() {
        LocalDate yesterday = LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(1);
        String targetDt = yesterday.format(FMT);

        log.info("[일간 스케줄러] {} 기준 일간 박스오피스 조회 및 저장 시작", yesterday);

        BoxOfficeRequestDTO req = BoxOfficeRequestDTO.builder()
            .targetDt(targetDt)
            .itemPerPage(10)
            .build();

        boxOfficeService.fetchAndStoreDailyBoxOffice(req);
    }
}
