package com.insidemovie.backend.api.movie.repository;

import com.insidemovie.backend.api.member.entity.Member;
import com.insidemovie.backend.api.movie.entity.Movie;
import com.insidemovie.backend.api.movie.entity.MovieLike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MovieLikeRepository extends JpaRepository<MovieLike, Long> {
    // 특정 회원이 좋아요 한 영화 목록을 페이징하여 조회
    Page<MovieLike> findByMember(Member member, Pageable pageable);

    // 영화 좋아요 여부 확인
    Optional<MovieLike> findByMovie_IdAndMember_Id(Long movieId, Long memberId);

    // 영화 + 유저 존재 여부 확인
    Boolean existsByMovie_IdAndMember_Id(Long movieId, Long memberId);

    // 좋아요 한 영화 개수 조회
    int countByMember_Id(Long memberId);
    Page<MovieLike> findByMovie(Member member, Pageable pageable);
    List<MovieLike> findByMember_Id(Long memberId);
    List<MovieLike> findByMovie(Movie movie);
}
