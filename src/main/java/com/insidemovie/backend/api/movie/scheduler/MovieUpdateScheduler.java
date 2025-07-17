package com.insidemovie.backend.api.movie.scheduler;


import com.insidemovie.backend.api.movie.service.MovieService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(name = "movie.update-enabled", havingValue = "true")
@RequiredArgsConstructor
public class MovieUpdateScheduler {
    private final MovieService movieService;

    @Scheduled(cron = "0 43 20 * * *") //
    public void updateMovies() {
        List<String> types = List.of("popular","now_playing");

        for (String type : types) {
            for (int page = 300; page <= 350; page++) { // 하루마다 전부 갱신하지 않아도 상위 400개만 확인
                movieService.fetchAndSaveMoviesByPage(type, page, false); // false = 변경 체크용
                try {
                    log.info("타입 "+type+"📄 페이지 " + page + " 처리 중...");
                    Thread.sleep(250); // 초당 4건 수준
                } catch (InterruptedException e) {
                    log.error("❌ 에러: " + e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }log.info("✅ 모든 페이지 처리 완료");
        }
    }
}
