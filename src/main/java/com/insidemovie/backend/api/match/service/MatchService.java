package com.insidemovie.backend.api.match.service;

import com.insidemovie.backend.api.constant.EmotionType;
import com.insidemovie.backend.api.match.entity.Match;
import com.insidemovie.backend.api.match.entity.MovieMatch;
import com.insidemovie.backend.api.match.repository.MatchRepository;
import com.insidemovie.backend.api.match.repository.MovieMatchRepository;
import com.insidemovie.backend.api.movie.entity.Movie;
import com.insidemovie.backend.api.movie.repository.MovieRepository;
import com.insidemovie.backend.common.exception.InternalServerException;
import com.insidemovie.backend.common.response.ErrorStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
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

    // 대결 생성
    @Transactional
    public void createMatch() {
        EmotionType[] emotions = EmotionType.values();

        // 랜덤 감정 값에 따른 영화 리스트
        int idx = random.nextInt(emotions.length);
        String emotion = String.valueOf(emotions[idx]);
        List<Movie> movieList = movieRepository.findTop3ByEmotion(emotion);

        // 몇 번째 match인지 계산
        Integer matchNumber = (int) matchRepository.count() + 1;

        // 매치 저장
        Match match = Match.builder()
                .matchDate(LocalDate.now())
                .matchNumber(matchNumber)
                .build();
        matchRepository.save(match);

        // 대결 정보 저장
        List<MovieMatch> movieMatchList = movieList.stream()
                .map(movie -> MovieMatch.builder()
                    .voteCount(0L)
                    .movie(movie)
                    .match(match)
                    .build())
                .toList();
        movieMatchRepository.saveAll(movieMatchList);
    }

    // 대결 종료
    @Transactional
    public void closeMatch() {
        // 마지막 매치 조회
        Optional<Match> match = matchRepository.findTopByOrderByMatchNumberDesc();
        // 매치가 없는 경우 진행X
        if (match.isEmpty()) {
            return;
        }

        Match lastMatch = match.get();

        // 마지막 매치의 영화 조회
        List<MovieMatch> movieMatches = movieMatchRepository.findByMatchId(lastMatch.getId());

        // 우승 영화
        MovieMatch winner = movieMatches.stream()
                .sorted(Comparator
                        .comparing(MovieMatch::getVoteCount, Comparator.reverseOrder())
                        .thenComparing(m -> m.getMovie().getRating(), Comparator.reverseOrder()))
                .findFirst()
                .orElse(null);

        if (winner != null) {
            lastMatch.setWinnerId(winner.getId());
            matchRepository.save(lastMatch);
        } else {
            throw new InternalServerException(ErrorStatus.FAIL_FIND_WINNER.getMessage());
        }
    }
}
