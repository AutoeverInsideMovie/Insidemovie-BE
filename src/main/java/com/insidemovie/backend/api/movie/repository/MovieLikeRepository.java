package com.insidemovie.backend.api.movie.repository;

import com.insidemovie.backend.api.member.entity.Member;
import com.insidemovie.backend.api.movie.entity.MovieLike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieLikeRepository extends JpaRepository<MovieLike, Long> {
    // 특정 회원이 좋아요 한 영화 목록을 페이징하여 조회
    Page<MovieLike> findByMember(Member member, Pageable pageable);
}
