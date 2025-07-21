package com.insidemovie.backend.api.movie.service;

import com.insidemovie.backend.api.constant.EmotionType;
import com.insidemovie.backend.api.member.dto.emotion.EmotionAvgDTO;
import com.insidemovie.backend.api.member.entity.Member;
import com.insidemovie.backend.api.member.repository.MemberRepository;
import com.insidemovie.backend.api.member.service.MemberService;
import com.insidemovie.backend.api.movie.dto.MyMovieResponseDTO;
import com.insidemovie.backend.api.movie.dto.PageResDto;
import com.insidemovie.backend.api.movie.entity.Movie;
import com.insidemovie.backend.api.movie.entity.MovieEmotionSummary;
import com.insidemovie.backend.api.movie.entity.MovieLike;
import com.insidemovie.backend.api.movie.repository.MovieEmotionSummaryRepository;
import com.insidemovie.backend.api.movie.repository.MovieLikeRepository;
import com.insidemovie.backend.api.movie.repository.MovieRepository;
import com.insidemovie.backend.api.review.repository.ReviewRepository;
import com.insidemovie.backend.api.review.service.ReviewService;
import com.insidemovie.backend.common.exception.NotFoundException;
import com.insidemovie.backend.common.response.ErrorStatus;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class MovieLikeService {
    private final MemberRepository memberRepository;
    private final MovieLikeRepository movieLikeRepository;
    private final MovieRepository movieRepository;
    private final MovieService movieService;
    private final ReviewRepository reviewRepository;
    private final MemberService memberService;

    // 좋아요 한 영화 목록 조회
    public PageResDto<MyMovieResponseDTO> getMyMovies(String memberEmail, Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        // 사용자 조회
        Member member = memberRepository.findByEmail(memberEmail)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBERID_EXCEPTION.getMessage()));

        // 영화 목록 조회
        Page<MovieLike> myMovies = movieLikeRepository.findByMember(member, pageable);

        Page<MyMovieResponseDTO> dto = myMovies.map(movielike ->{
            Movie movie = movielike.getMovie();
            EmotionAvgDTO avg = movieService.getMovieEmotionSummary(movie.getId());
            EmotionType mainEmotion = avg.getRepEmotionType();

            Double ratingAvg = reviewRepository.findAverageByMovieId(movie.getId());
            BigDecimal rounded;
            if(ratingAvg==null || ratingAvg==0.00){
                rounded=BigDecimal.ZERO.setScale(2);
            }else{
                rounded= BigDecimal.valueOf(ratingAvg)
                        .setScale(2, RoundingMode.HALF_UP);
            }

            // mainEmotion에 해당하는 수치 꺼내기
            double emainEmotionValue = switch (mainEmotion) {
                case JOY -> avg.getJoy();
                case SADNESS -> avg.getSadness();
                case ANGER -> avg.getAnger();
                case FEAR -> avg.getFear();
                case DISGUST -> avg.getDisgust();
                case NONE -> 0.0;
            };

            return MyMovieResponseDTO.builder()
                    .movieReactionId(movielike.getId())
                    .movieId(movie.getId())
                    .posterPath(movie.getPosterPath())
                    .title(movie.getTitle())
                    .voteAverage(movie.getVoteAverage())
                    .mainEmotion(mainEmotion)
                    .mainEmotionValue(emainEmotionValue)
                    .ratingAvg(rounded)
                    .build();
        });
        return new PageResDto<>(dto);
    }

    @Transactional
    public void toggleMovieLike(Long movieId, String memberEmail) {
        Member member = memberRepository.findByEmail(memberEmail)
            .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBERID_EXCEPTION.getMessage()));
        Movie movie = movieRepository.findById(movieId)
            .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MOVIE_EXCEPTION.getMessage()));

        Optional<MovieLike> existing = movieLikeRepository.findByMovie_IdAndMember_Id(movieId, member.getId());
        if (existing.isPresent()) {
            movieLikeRepository.delete(existing.get());
        } else {
            movieLikeRepository.save(MovieLike.builder()
                .movie(movie)
                .member(member)
                .build());
        }

        memberService.updateEmotionSummaryByLikedMovies(member.getId());
    }
}