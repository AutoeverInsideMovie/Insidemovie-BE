package com.insidemovie.backend.api.match.service;

import com.insidemovie.backend.api.constant.EmotionType;
import com.insidemovie.backend.api.match.entity.Match;
import com.insidemovie.backend.api.match.entity.MovieMatch;
import com.insidemovie.backend.api.match.repository.MatchRepository;
import com.insidemovie.backend.api.match.repository.MovieMatchRepository;
import com.insidemovie.backend.api.movie.entity.Movie;
import com.insidemovie.backend.api.movie.repository.MovieRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchService {
    private final MovieMatchRepository movieMatchRepository;
    private final MatchRepository matchRepository;
    private final MovieRepository movieRepository;
    private final Random random = new Random();

    @Transactional
    public void createMatch() {
        EmotionType[] emotions = EmotionType.values();

        // 랜덤 감정 값에 따른 영화 리스트
        int idx = random.nextInt(emotions.length);
        String emotion = String.valueOf(emotions[idx]);
        List<Movie> movieList = movieRepository.findTop3ByEmotion(emotion);

        // 몇 번째 match인지 계산
        Integer matchNumber = (int) matchRepository.count() + 1;

        Match match = Match.builder()
                .matchDate(LocalDate.now())
                .matchNumber(matchNumber)
                .build();
        matchRepository.save(match);

        List<MovieMatch> movieMatchList = movieList.stream()
                .map(movie -> MovieMatch.builder()
                    .voteCount(0L)
                    .movieRank(0)
                    .movie(movie)
                    .match(match)
                    .build())
                .toList();
        movieMatchRepository.saveAll(movieMatchList);
    }

    @Transactional
    public void closeMatch() {
        Optional<MovieMatch> lastMatch = movieMatchRepository.find
    }

}
