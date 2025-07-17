package com.insidemovie.backend.api.review.repository;

import com.insidemovie.backend.api.member.entity.Member;
import com.insidemovie.backend.api.movie.entity.Movie;
import com.insidemovie.backend.api.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 특정 영화에 대한 리뷰 페이징 조회
    Page<Review> findByMovie(Movie movie, Pageable pageable);

    // 특정 회원이 특정 영화에 작성한 리뷰
    Optional<Review> findByMemberAndMovie(Member member, Movie movie);

    Page<Review> findByMember(Member member, Pageable pageable);

    long countByMember(Member member);

    // 리뷰 좋아요 수 증가
    @Modifying
    @Transactional
    @Query("UPDATE Review r SET r.likeCount = r.likeCount + 1 WHERE r.id = :reviewId")
    void incrementLikeCount(@Param("reviewId") Long reviewId);

    // 리뷰 좋아요 수 감소
    @Modifying
    @Transactional
    @Query("UPDATE Review r SET r.likeCount = CASE WHEN r.likeCount > 0 THEN r.likeCount - 1 ELSE 0 END WHERE r.id = :reviewId")
    void decrementLikeCount(@Param("reviewId") Long reviewId);

    // 숨김 처리된 리뷰 수
    long countByIsConcealedTrue();

}
