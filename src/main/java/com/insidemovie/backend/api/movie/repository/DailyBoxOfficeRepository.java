package com.insidemovie.backend.api.movie.repository;

import com.insidemovie.backend.api.movie.entity.boxoffice.DailyBoxOfficeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;

public interface DailyBoxOfficeRepository extends JpaRepository<DailyBoxOfficeEntity, Long> {
    void deleteByTargetDate(LocalDate date);
    Page<DailyBoxOfficeEntity> findByTargetDate(LocalDate date, Pageable pageable);
    Optional<DailyBoxOfficeEntity> findByTargetDateAndMovieCd(LocalDate targetDate, String movieCd);

    Optional<DailyBoxOfficeEntity> findByMovie_TmdbMovieIdAndTargetDate(Long tmdbMovieId, LocalDate date);
}
