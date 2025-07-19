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
public class WeeklyBoxOfficeScheduler {

    private final BoxOfficeService boxOfficeService;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Scheduled(cron = "${scheduler.cron.weekly}", zone = "${scheduler.zone}")
    @Transactional
    public void fetchAndStoreWeeklyBoxOffice() {
        LocalDate lastMonday = LocalDate.now(ZoneId.of("Asia/Seoul")).minusWeeks(1);
        String targetDt = lastMonday.format(FMT);

        log.info("[주간 스케줄러] {} 기준 주간 박스오피스 조회 및 저장 시작", lastMonday);

        BoxOfficeRequestDTO req = BoxOfficeRequestDTO.builder()
            .targetDt(targetDt)
            .weekGb("0")
            .itemPerPage(10)
            .build();

        boxOfficeService.fetchAndStoreWeeklyBoxOffice(req);
    }
}
