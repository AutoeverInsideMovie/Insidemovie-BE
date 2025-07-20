package com.insidemovie.backend.api.movie.repository;

import com.insidemovie.backend.api.movie.entity.boxoffice.DailyBoxOfficeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyBoxOfficeRepository extends JpaRepository<DailyBoxOfficeEntity, Long> {

    Optional<DailyBoxOfficeEntity> findByTargetDateAndMovieCd(LocalDate targetDate, String movieCd);

    Optional<DailyBoxOfficeEntity> findByMovie_TmdbMovieIdAndTargetDate(Long tmdbMovieId, LocalDate targetDate);

    @Query("select max(d.targetDate) from DailyBoxOfficeEntity d")
    Optional<LocalDate> findLatestTargetDate();

    // (1) 특정 날짜 정렬 (movie_rank 가 varchar 이므로 cast)
    @Query(value = """
        select * 
        from daily_box_office
        where target_date = :targetDate
        order by cast(movie_rank as unsigned)
        """, nativeQuery = true)
    List<DailyBoxOfficeEntity> findAllSortedByTargetDate(@Param("targetDate") LocalDate targetDate);

    // (2) 최신 날짜 + 정렬 한번에
    @Query(value = """
        select * 
        from daily_box_office
        where target_date = (select max(target_date) from daily_box_office)
        order by cast(movie_rank as unsigned)
        """, nativeQuery = true)
    List<DailyBoxOfficeEntity> findLatestSorted();
}