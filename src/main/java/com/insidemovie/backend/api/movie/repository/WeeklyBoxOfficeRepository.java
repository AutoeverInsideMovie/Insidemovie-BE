package com.insidemovie.backend.api.movie.repository;

import com.insidemovie.backend.api.movie.entity.boxoffice.WeeklyBoxOfficeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeeklyBoxOfficeRepository extends JpaRepository<WeeklyBoxOfficeEntity, Long> {
    // 특정 연도주차 레코드를 모두 삭제
    void deleteByYearWeekTime(String yearWeekTime);
}
