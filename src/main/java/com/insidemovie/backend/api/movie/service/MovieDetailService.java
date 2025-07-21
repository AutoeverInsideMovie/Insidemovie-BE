package com.insidemovie.backend.api.movie.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insidemovie.backend.api.member.entity.Member;
import com.insidemovie.backend.api.member.repository.MemberRepository;
import com.insidemovie.backend.api.movie.dto.MovieDetailResDto;
import com.insidemovie.backend.api.movie.entity.Movie;
import com.insidemovie.backend.api.movie.repository.MovieGenreRepository;
import com.insidemovie.backend.api.movie.repository.MovieLikeRepository;
import com.insidemovie.backend.api.movie.repository.MovieRepository;
import com.insidemovie.backend.api.review.repository.ReviewRepository;
import com.insidemovie.backend.common.exception.NotFoundException;
import com.insidemovie.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class MovieDetailService {

    private final ObjectMapper objectMapper;
    private final MovieRepository movieRepository;
    private final MovieGenreRepository movieGenreRepository;
    private final MovieLikeRepository movieLikeRepository;
    private final MemberRepository memberRepository;
    private final ReviewRepository reviewRepository;

    /**
     * 비로그인 사용자 영화 상세
     */
    public MovieDetailResDto getMovieDetail(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MOVIE_EXCEPTION.getMessage()));

        return buildDetailDto(movie, false);
    }

    /**
     * 로그인 사용자 영화 상세 (좋아요 여부 포함)
     */
    public MovieDetailResDto getMovieDetail(Long id, String userEmail) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MOVIE_EXCEPTION.getMessage()));

        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBERID_EXCEPTION.getMessage()));

        boolean isLike = movieLikeRepository.existsByMovie_IdAndMember_Id(movie.getId(), member.getId());
        return buildDetailDto(movie, isLike);
    }

    /**
     * 공통 DTO 빌더
     */
    private MovieDetailResDto buildDetailDto(Movie movie, boolean isLike) {
        // 장르
        List<String> genreNames = movieGenreRepository.findByMovieId(movie.getId())
                .stream()
                .map(mg -> mg.getGenreType().name())
                .toList();
        Double ratingAvg = reviewRepository.findAverageByMovieId(movie.getId());
        BigDecimal rounded;
        if(ratingAvg==null || ratingAvg==0.00){
            rounded=BigDecimal.ZERO.setScale(2);
        }else{
            rounded= BigDecimal.valueOf(ratingAvg)
                    .setScale(2, RoundingMode.HALF_UP);
        }

        MovieDetailResDto dto = new MovieDetailResDto();
        dto.setId(movie.getId());
        dto.setTitle(movie.getTitle());
        dto.setTitleEn(movie.getTitleEn());
        dto.setOverview(movie.getOverview());
        dto.setPosterPath(movie.getPosterPath());
        dto.setBackdropPath(movie.getBackdropPath());
        dto.setVoteAverage(movie.getVoteAverage());
        dto.setOriginalLanguage(movie.getOriginalLanguage());
        dto.setIsLike(isLike);
        dto.setGenre(genreNames);
        dto.setRatingAvg(rounded);

        // 배우 / 감독 / OTT 파싱
        dto.setActors(readArrayFlexible(movie.getActors()));          // List<String>
        dto.setDirector(readArrayFlexible(movie.getDirectors()));     // List<String>
        dto.setOttProviders(readArrayFlexible(movie.getOttProviders()));

        dto.setRating(movie.getRating());
        dto.setRuntime(movie.getRuntime());
        dto.setStatus(movie.getStatus());

        // releaseDate (엔티티 타입에 따라 처리)
        if (movie.getReleaseDate() != null) {
            // 엔티티가 LocalDate 라면
            dto.setReleaseDate(movie.getReleaseDate().toString());
            // 만약 LocalDateTime 이라면: dto.setReleaseDate(movie.getReleaseDate().toLocalDate().toString());
        } else {
            dto.setReleaseDate(null);
        }
        return dto;
    }

    /**
     * actors / directors / ottProviders 컬럼이
     * - JSON 문자열: ["A","B"]
     * - toString() 형태: [A, B]
     * 둘 다 올 수 있으므로 유연하게 처리
     */
    private List<String> readArrayFlexible(String raw) {
        if (raw == null || raw.isBlank()) return List.of();

        // 우선 JSON 시도
        if (raw.startsWith("[") && raw.endsWith("]")) {
            // JSON 으로 한 번 파싱 시도
            try {
                return objectMapper.readValue(raw, new TypeReference<List<String>>() {});
            } catch (Exception ignore) {
                // 실패하면 아래 fallback
            }
            // fallback: toString() 형태 파싱
            return Arrays.stream(raw.substring(1, raw.length() - 1).split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        }
        // 그냥 단일 값
        return List.of(raw.trim());
    }
}
