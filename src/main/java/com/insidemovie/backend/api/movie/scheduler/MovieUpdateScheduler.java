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
            int totalPages = movieService.fetchTotalPages(type);
            if (totalPages <= 0) {
                log.warn("타입 '{}' totalPages 계산 실패 → 건너뜀", type);
                continue;
            }
            log.info("타입 '{}' totalPages={}", type, totalPages);

            for (int page = 1; page <= totalPages; page++) {
                log.info("타입 '{}' 페이지 {} 처리 시작", type, page);
                boolean hasData = movieService.fetchAndSaveMoviesByPage(type, page, false);
                if (!hasData) {
                    log.info("타입 '{}' 페이지 {} 이후 데이터 없음 → 조기 종료", type, page);
                    break;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("인터럽트됨. 타입 '{}' page {} 중단", type, page);
                    return;
                }
            }
            log.info("타입 '{}' 처리 완료", type);
        }
    }
}
