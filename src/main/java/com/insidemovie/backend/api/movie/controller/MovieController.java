package com.insidemovie.backend.api.movie.controller;

import com.insidemovie.backend.api.movie.dto.movie.MoviePagedResponseDTO;
import com.insidemovie.backend.api.movie.dto.movie.MovieResponseDTO;
import com.insidemovie.backend.api.movie.entity.Movie;
import com.insidemovie.backend.api.movie.service.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 영화 관련 API를 제공하는 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/movies")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Movies", description = "영화 정보를 조회 및 검색하는 API")
public class MovieController {
    private final MovieService movieService;

    @GetMapping("/search")
    @Operation(summary = "영화 제목 검색", description = "제목에 키워드를 포함하는 영화를 검색하며, DB에 없으면 TMDB API에서 조회 후 저장합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "검색 성공",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = MovieResponseDTO.class)))
    })
    public ResponseEntity<MoviePagedResponseDTO<MovieResponseDTO>> search(
        @Parameter(description = "검색할 키워드", required = true)
        @RequestParam("q") String keyword,
        @PageableDefault(size = 20)
        Pageable pageable
    ) {
        Page<MovieResponseDTO> page = movieService
        .searchOrFetchMoviesByTitle(keyword, pageable)
        .map(MovieResponseDTO::fromEntity);

        MoviePagedResponseDTO<MovieResponseDTO> response = new MoviePagedResponseDTO<>(
        page.getContent(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages()
    );

    return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "단일 영화 상세 조회", description = "영화의 상세 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = MovieResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "영화를 찾을 수 없음")
    })
    public ResponseEntity<MovieResponseDTO> getOne(
        @Parameter(description = "영화 ID", required = true)
        @PathVariable Long id
    ) {
        Movie movie = movieService.getMovie(id);
        return ResponseEntity.ok(MovieResponseDTO.fromEntity(movie));
    }
}
