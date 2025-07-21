package com.insidemovie.backend.api.match.service;

import com.insidemovie.backend.api.constant.EmotionType;
import com.insidemovie.backend.api.match.dto.WinnerHistoryDto;
import com.insidemovie.backend.api.match.entity.Match;
import com.insidemovie.backend.api.match.entity.MovieMatch;
import com.insidemovie.backend.api.match.entity.Vote;
import com.insidemovie.backend.api.match.repository.MatchRepository;
import com.insidemovie.backend.api.match.repository.MovieMatchRepository;
import com.insidemovie.backend.api.match.repository.VoteRepository;
import com.insidemovie.backend.api.member.entity.Member;
import com.insidemovie.backend.api.member.repository.MemberRepository;
import com.insidemovie.backend.api.movie.dto.MovieDetailSimpleResDto;
import com.insidemovie.backend.api.movie.dto.emotion.MovieEmotionResDTO;
import com.insidemovie.backend.api.movie.entity.Movie;
import com.insidemovie.backend.api.movie.entity.MovieEmotionSummary;
import com.insidemovie.backend.api.movie.repository.MovieRepository;
import com.insidemovie.backend.api.review.repository.ReviewRepository;
import com.insidemovie.backend.common.exception.InternalServerException;
import com.insidemovie.backend.common.exception.NotFoundException;
import com.insidemovie.backend.common.response.ErrorStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
// TODO: 지금 emotion table이 없어서 주석처리 해놨음 주석 풀어야됨!!!
public class MatchService {
    private final MovieMatchRepository movieMatchRepository;
    private final MatchRepository matchRepository;
    private final MovieRepository movieRepository;
    private final VoteRepository voteRepository;
    private final MemberRepository memberRepository;
    private final Random random = new Random();
    private final ReviewRepository reviewRepository;

    // 대결 생성
    @Transactional
    public void createMatch() {
        EmotionType[] emotions = EmotionType.values();

        // 랜덤 감정 값에 따른 영화 리스트
        int idx = random.nextInt(emotions.length);
        EmotionType emotion = emotions[idx];
        List<Movie> movieList = movieRepository.findTop3ByEmotion(emotion);

        if (movieList.size() < 3) {
            log.warn("감정 [{}]에 해당하는 영화가 3개 미만입니다. 랜덤 영화로 대체합니다.", emotion);
            movieList = movieRepository.find3Movie(emotion);
            String titles = movieList.stream()
                    .map(Movie::getTitle)
                    .toList()
                    .toString();

            log.info("대체된 랜덤 영화 리스트: {}", titles);
        }
        // 몇 번째 match인지 계산
        Integer matchNumber = (int) matchRepository.count() + 1;

        // 매치 저장
        Match match = Match.builder()
                .matchDate(LocalDate.now())
                .matchNumber(matchNumber)
                .build();
        matchRepository.save(match);

        // 매치 생성 시 isMatched를 true로 업데이트
        movieList.forEach(movie -> movie.setIsMatched(true));
        movieRepository.saveAll(movieList);

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
        Match lastMatch = matchRepository.findTopByOrderByMatchNumberDesc()
                .orElseThrow(() -> new NotFoundException((ErrorStatus.NOT_FOUND_MATCH.getMessage())));

        // 마지막 매치의 영화 조회
        List<MovieMatch> movieMatches = movieMatchRepository.findByMatchId(lastMatch.getId());

        // 우승 영화
        MovieMatch winner = movieMatches.stream()
                .sorted(Comparator
                        .comparing(MovieMatch::getVoteCount, Comparator.reverseOrder())
                        .thenComparing(m -> m.getMovie().getRating(), Comparator.reverseOrder()))
                .findFirst()
                .orElseThrow(() -> new InternalServerException(ErrorStatus.NOT_FOUND_WINNER.getMessage()));

        lastMatch.setWinnerId(winner.getMovie().getId());
        matchRepository.save(lastMatch);
    }

    // 대결 투표
    @Transactional
    public void voteMatch(Long movieId, String memberEmail) {
        // 사용자 조회
        Member member = memberRepository.findByEmail(memberEmail)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBERID_EXCEPTION.getMessage()));

        // 영화 조회
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MOVIE_EXCEPTION.getMessage()));

        // 최근 매치
        Match lastMatch = matchRepository.findTopByOrderByMatchNumberDesc()
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MATCH.getMessage()));

        // 매치 정보
        MovieMatch movieMatch = movieMatchRepository.findByMatchIdAndMovieId(lastMatch.getId(), movieId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MATCH.getMessage()));

        // 투표 여부 확인
        Boolean isVoted = voteRepository.existsByMatchIdAndMemberId(lastMatch.getId(), member.getId());
        if (isVoted) {
            throw new IllegalStateException(ErrorStatus.DUPLICATE_VOTE_EXCEPTION.getMessage());
        }

        movieMatch.setVoteCount(movieMatch.getVoteCount() + 1);
        Vote vote = Vote.builder()
                .votedAt(LocalDateTime.now())
                .member(member)
                .match(lastMatch)
                .movie(movie)
                .build();
        voteRepository.save(vote);
    }

    // 영화 대결 조회
    public List<MovieDetailSimpleResDto> getMatchDetail(){
        List<MovieDetailSimpleResDto> response = new ArrayList<>();

        // 최근 매치
        Match lastMatch = matchRepository.findTopByOrderByMatchNumberDesc()
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MATCH.getMessage()));

        // 매치 내역 조회
        List<MovieMatch> movieMatch = movieMatchRepository.findByMatchId(lastMatch.getId());

        for (MovieMatch mm : movieMatch) {
            Movie movie = mm.getMovie();
            MovieEmotionSummary movieEmotion = movie.getEmotions();

            Double ratingAvg = reviewRepository.findAverageByMovieId(movie.getId());
            BigDecimal rounded;
            if(ratingAvg==null || ratingAvg==0.00){
                rounded=BigDecimal.ZERO.setScale(2);
            }else{
                rounded= BigDecimal.valueOf(ratingAvg)
                        .setScale(2, RoundingMode.HALF_UP);
            }

            MovieEmotionResDTO emotionDto = MovieEmotionResDTO.builder()
                    .joy(movieEmotion.getJoy())
                    .anger(movieEmotion.getAnger())
                    .sadness(movieEmotion.getSadness())
                    .fear(movieEmotion.getFear())
                    .disgust(movieEmotion.getDisgust())
                    .dominantEmotion(movieEmotion.getDominantEmotion())
                    .build();

            MovieDetailSimpleResDto dto = MovieDetailSimpleResDto.builder()
                    .id(movie.getId())
                    .title(movie.getTitle())
                    .posterPath(movie.getPosterPath())
                    .voteAverage(movie.getVoteAverage())
                //     .emotion(emotionDto)
                    .ratingAvg(rounded)
                    .build();
            response.add(dto);
        }
        return response;
    }

    // 역대 우승 영화 조회
    public List<WinnerHistoryDto> getWinnerHistory() {
        List<Match> matches = matchRepository.findAll();
        List<WinnerHistoryDto> response = new ArrayList<>();

        for (Match match : matches) {
            if (match.getWinnerId() == null) continue;
            Movie movie = movieRepository.findById(match.getWinnerId())
                    .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MOVIE_EXCEPTION.getMessage()));

            Double ratingAvg = reviewRepository.findAverageByMovieId(movie.getId());
            BigDecimal rounded;
            if(ratingAvg==null || ratingAvg==0.00){
                rounded=BigDecimal.ZERO.setScale(2);
            }else{
                rounded= BigDecimal.valueOf(ratingAvg)
                        .setScale(2, RoundingMode.HALF_UP);
            }

            MovieEmotionSummary movieEmotion = movie.getEmotions();
            MovieEmotionResDTO emotionDto = MovieEmotionResDTO.builder()
                    .joy(movieEmotion.getJoy())
                    .anger(movieEmotion.getAnger())
                    .sadness(movieEmotion.getSadness())
                    .fear(movieEmotion.getFear())
                    .disgust(movieEmotion.getDisgust())
                    .dominantEmotion(movieEmotion.getDominantEmotion())
                    .build();

            MovieDetailSimpleResDto movieDto = MovieDetailSimpleResDto.builder()
                    .id(movie.getId())
                    .title(movie.getTitle())
                    .posterPath(movie.getPosterPath())
                    .voteAverage(movie.getVoteAverage())
                    .ratingAvg(rounded)
                //     .emotion(emotionDto)
                    .build();

            WinnerHistoryDto winnerHistoryDto = WinnerHistoryDto.builder()
                    .matchNumber(match.getMatchNumber())
                    .matchDate(match.getMatchDate())
                    .movie(movieDto)
                    .build();
            response.add(winnerHistoryDto);
        }
        return response;
    }
}
