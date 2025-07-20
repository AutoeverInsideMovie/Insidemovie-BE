package com.insidemovie.backend.api.movie.repository;

import com.insidemovie.backend.api.movie.entity.boxoffice.WeeklyBoxOfficeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface WeeklyBoxOfficeRepository extends JpaRepository<WeeklyBoxOfficeEntity, Long> {

    Optional<WeeklyBoxOfficeEntity> findByYearWeekTimeAndMovieCd(String yearWeekTime, String movieCd);

    Optional<WeeklyBoxOfficeEntity> findByMovie_TmdbMovieIdAndYearWeekTime(Long tmdbMovieId, String yearWeekTime);

    @Query(value = """
        select * 
        from weekly_box_office
        where year_week_time = :yearWeek
        order by cast(movie_rank as unsigned)
        """, nativeQuery = true)
    List<WeeklyBoxOfficeEntity> findAllSortedByYearWeek(@Param("yearWeek") String yearWeek);

    @Query(value = """
        select * 
        from weekly_box_office
        where year_week_time = (select max(year_week_time) from weekly_box_office)
        order by cast(movie_rank as unsigned)
        """, nativeQuery = true)
    List<WeeklyBoxOfficeEntity> findLatestSorted();
}
