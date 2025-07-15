package com.insidemovie.backend.api.movie.service;

import com.insidemovie.backend.api.movie.dto.MovieListResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class MovieServiceKofic {
    private final WebClient webClient;
    private final String apiKey;

    public MovieServiceKofic(WebClient kobisWebClient,
                        @Value("${kobis.api.key}") String apiKey) {
        this.webClient = kobisWebClient;
        this.apiKey = apiKey;
    }
    /**
     * 영화명으로 검색해서 Mono<MovieListResponse>를 반환
     */
    public Mono<MovieListResponse> searchMovie(String movieName) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/movie/searchMovieList.json")
                        .queryParam("key", apiKey)
                        .queryParam("movieNm", movieName)
                        .build()
                )
                .retrieve()
                .onStatus(status -> status.value() == 401,
                        resp -> {
                            // 에러 바디 로깅
                            return resp.bodyToMono(String.class)
                                    .flatMap(body -> {
                                        System.err.println("401 에러 바디: " + body);
                                        return Mono.error(new RuntimeException("인증 실패: API 키를 확인하세요"));
                                    });
                        })
                .onStatus(status -> status.is4xxClientError(),
                        resp -> Mono.error(new RuntimeException("잘못된 요청")))
                .onStatus(status -> status.is5xxServerError(),
                        resp -> Mono.error(new RuntimeException("서버 에러")))
                .bodyToMono(MovieListResponse.class);
    }

    public Mono<MovieListResponse> searchMovieList(String curPage, String itemPerPage){
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/movie/searchMovieList.json")
                        .queryParam("key", apiKey)
                        .queryParam("curPage", curPage)
                        .queryParam("itemPerPage", itemPerPage)
                        .build()
                )
                .retrieve()
                .onStatus(status -> status.value() == 401,
                        resp -> {
                            // 에러 바디 로깅
                            return resp.bodyToMono(String.class)
                                    .flatMap(body -> {
                                        System.err.println("401 에러 바디: " + body);
                                        return Mono.error(new RuntimeException("인증 실패: API 키를 확인하세요"));
                                    });
                        })
                .onStatus(status -> status.is4xxClientError(),
                        resp -> Mono.error(new RuntimeException("잘못된 요청")))
                .onStatus(status -> status.is5xxServerError(),
                        resp -> Mono.error(new RuntimeException("서버 에러")))
                .bodyToMono(MovieListResponse.class);
    }
    /**
     * 블로킹 방식으로 동기 호출이 필요할 때 사용
     */
    public MovieListResponse searchMovieBlocking(String movieName) {
        return searchMovie(movieName).block();
    }
}
