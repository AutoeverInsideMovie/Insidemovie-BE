package com.insidemovie.backend.api.movie.repository;

import com.insidemovie.backend.api.movie.entity.boxoffice.WeeklyBoxOfficeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WeeklyBoxOfficeRepository extends JpaRepository<WeeklyBoxOfficeEntity, Long> {
    Page<WeeklyBoxOfficeEntity> findByYearWeekTime(String yearWeekTime, Pageable pageable);
    Optional<WeeklyBoxOfficeEntity> findFirstByOrderByYearWeekTimeDesc();
    Optional<WeeklyBoxOfficeEntity> findByYearWeekTimeAndMovieCd(String yearWeekTime, String movieCd);
}
