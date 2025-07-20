package com.insidemovie.backend.api.match.scheduler;

import com.insidemovie.backend.api.match.service.MatchService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@AllArgsConstructor
@Component
public class MatchScheduler {
    private final MatchService matchService;

    @Scheduled(cron = "${match.cron.weekly}", zone = "Asia/Seoul")
    public void generateMovieMatch() {
        log.info("지난 주 영화 대결 종료");
        matchService.closeMatch();
        log.info("영화 대결 생성");
        matchService.createMatch();
    }
}
