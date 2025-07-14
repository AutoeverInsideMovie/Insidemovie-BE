package com.insidemovie.backend.api.movie.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name="search_cahe")
@NoArgsConstructor
@AllArgsConstructor
public class SearchCache {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "search_id")
    private Long id;

    private String title;
    private String overview;
    private String posterUrl;
    private Double rating; //평점
    private Integer voteCount; //몇명이 평점을 남겼는지
    private LocalDate releaseDate; //개봉일

    private LocalDateTime refreshedAt; //마지막 갱신 시간

}
