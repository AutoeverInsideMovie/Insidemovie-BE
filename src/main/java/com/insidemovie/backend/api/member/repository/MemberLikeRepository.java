package com.insidemovie.backend.api.member.repository;

import com.insidemovie.backend.api.movie.entity.MovieLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberLikeRepository extends JpaRepository<MovieLike, Long> {

    /**
     * 특정 영화(movieId)에 좋아요를 누른 회원들의 ID 목록을 반환
     */
    @Query("SELECT ml.member.id FROM MovieLike ml WHERE ml.movie.id = :movieId")
    List<Long> findMemberIdsByMovieId(@Param("movieId") Long movieId);
}