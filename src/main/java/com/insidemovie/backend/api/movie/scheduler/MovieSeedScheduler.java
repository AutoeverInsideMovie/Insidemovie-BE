package com.insidemovie.backend.api.movie.scheduler;

import com.insidemovie.backend.api.movie.service.MovieService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;
@Slf4j
@Component
@ConditionalOnProperty(name = "movie.seed-enabled", havingValue = "true")
public class MovieSeedScheduler {
    private final MovieService movieService;

    private static final int MAX_PAGE = 500;
    private static final List<String> TYPES = List.of("popular","top_rated","now_playing","upcoming");

    private int currentPage = 250;
    private int typeIndex=0;
    public MovieSeedScheduler(MovieService movieService){
        this.movieService=movieService;
    }
    @Scheduled(fixedDelay = 10000)
    public void seedMovies(){
        if (typeIndex >= TYPES.size()) {
            log.info("✅ 모든 페이지 처리 완료");
            return; // 완료
        }
        try {
            log.info("타입 "+TYPES.get(typeIndex)+"📄 페이지 " + currentPage + " 처리 중...");
            String type = TYPES.get(typeIndex);
            movieService.fetchAndSaveMoviesByPage(type, currentPage, true); // true = 초기 시딩용
            currentPage++;

            if (currentPage > MAX_PAGE) {
                currentPage = 1;
                typeIndex++;
            }
        } catch (Exception e){
            log.error("❌ 에러: " + e.getMessage());
        }
    }
}
