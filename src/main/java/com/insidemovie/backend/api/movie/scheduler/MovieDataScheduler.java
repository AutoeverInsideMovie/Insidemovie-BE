package com.insidemovie.backend.api.movie.scheduler;

import com.insidemovie.backend.api.movie.service.MoviesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
@Slf4j
@Component
public class MovieDataScheduler {
    private final MoviesService moviesService;

    public MovieDataScheduler(MoviesService moviesService){
        this.moviesService= moviesService;
    }
    //매일 오전 6시(Asia/Seoul) 에 한 번씩 실행
    //cron 포맷: 초 분 시 일 월 요일
    @Scheduled(cron = "0 56 20 * * *", zone="Asia/Seoul")
    public void fetchAndStoreMovies(){
        log.info("[SCHEDULER] 영화 데이터 갱신 시작 (task-thread={})",
                Thread.currentThread().getName());
        moviesService.fetchAndSaveAllMovies();
        log.info("[SCHEDULER] 영화 데이터 갱신 완료");
    }
}
