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

    @Scheduled(cron = "${scheduler.cron.request_movie}")
    public void updateMovies() {
        List<String> types = List.of("popular", "now_playing", "upcoming", "top_rated");

        for (String type : types) {
            for (int page = 1; page <= 499; page++) {
                log.info("영화 타입 '{}' 페이지 {} 처리 시작", type, page);
                movieService.fetchAndSaveMoviesByPage(type, page, false);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    log.error("영화 업데이트 처리 중 예외 발생: {}", e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }
            log.info("영화 타입 '{}' 모든 페이지 처리 완료", type);
        }
    }
}
