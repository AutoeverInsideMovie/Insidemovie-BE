package com.insidemovie.backend.api.movie.repository;

import com.insidemovie.backend.api.movie.entity.SearchCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface SearchRepository extends JpaRepository<SearchCache,Long> {
    //제목으로 검색
    List<SearchCache> findByTitleContainingIgnoreCase(String title); //대소문자 구분 없이
}
