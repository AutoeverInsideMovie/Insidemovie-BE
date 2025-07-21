package com.insidemovie.backend.api.review.repository;

import com.insidemovie.backend.api.member.dto.emotion.EmotionAvgDTO;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 특정 영화에 대한 리뷰 페이징 조회
    Page<Review> findByMovie(Movie movie, Pageable pageable);

    // 특정 회원이 특정 영화에 작성한 리뷰 (중복 방지)
    Optional<Review> findByMemberAndMovie(Member member, Movie movie);

    // 특정 회원이 작성한 리뷰 목록을 페이징하여 조회
    Page<Review> findByMember(Member member, Pageable pageable);

    // 내 리뷰 제외
    Page<Review> findByMovieAndIdNot(Movie movie, Long id, Pageable pageable);

    // 리뷰 목록에서 숨김 리뷰 제외하고 조회
    Page<Review> findByMovieAndIsConcealedFalse(Movie movie, Pageable pageable);
    Page<Review> findByMovieAndIdNotAndIsConcealedFalse(Movie movie, Long id, Pageable pageable);


    // 특정 회원이 작성한 리뷰 개수를 반환
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

    // 누적 리뷰 수 (특정 시점까지)
    long countByCreatedAtLessThan(LocalDateTime dateTime);

    // 일별 작성 리뷰 수
    @Query("""
        SELECT DATE(r.createdAt), COUNT(r)
        FROM Review r
        WHERE r.createdAt >= :start AND r.createdAt < :end
        GROUP BY DATE(r.createdAt)
        ORDER BY DATE(r.createdAt)
    """)
    List<Object[]> countReviewsDaily(@Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end);

    // 월별 작성 수
    @Query("""
        SELECT FUNCTION('DATE_FORMAT', r.createdAt, '%Y-%m'), COUNT(r)
        FROM Review r
        WHERE r.createdAt >= :start AND r.createdAt < :end
        GROUP BY FUNCTION('DATE_FORMAT', r.createdAt, '%Y-%m')
        ORDER BY FUNCTION('DATE_FORMAT', r.createdAt, '%Y-%m')
    """)
    List<Object[]> countReviewsMonthly(@Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.movie.id =:movieId")
    Double findAverageByMovieId(@Param("movieId") Long movieId);

    /**
     * 특정 영화에 달린 모든 리뷰의 감정 점수를 평균 내고,
     * 평균값이 가장 높은 감정 타입을 repEmotionType으로 삼아 DTO로 반환
     */
    @Query("""
    SELECT new com.insidemovie.backend.api.member.dto.emotion.EmotionAvgDTO(
        AVG(e.joy), 
        AVG(e.sadness), 
        AVG(e.anger), 
        AVG(e.fear), 
        AVG(e.disgust),
        CASE
            WHEN AVG(e.joy)     >= AVG(e.sadness) AND AVG(e.joy)     >= AVG(e.anger) AND AVG(e.joy)     >= AVG(e.fear)    AND AVG(e.joy)     >= AVG(e.disgust) THEN com.insidemovie.backend.api.constant.EmotionType.JOY
            WHEN AVG(e.sadness) >= AVG(e.anger)   AND AVG(e.sadness) >= AVG(e.fear)    AND AVG(e.sadness) >= AVG(e.disgust)                    THEN com.insidemovie.backend.api.constant.EmotionType.SADNESS
            WHEN AVG(e.anger)   >= AVG(e.fear)    AND AVG(e.anger)   >= AVG(e.disgust)                                          THEN com.insidemovie.backend.api.constant.EmotionType.ANGER
            WHEN AVG(e.fear)    >= AVG(e.disgust)                                                                     THEN com.insidemovie.backend.api.constant.EmotionType.FEAR
            ELSE                                                                                                         com.insidemovie.backend.api.constant.EmotionType.DISGUST
        END
    )
    FROM Emotion e
    WHERE e.review.movie.id = :movieId
    """)
    EmotionAvgDTO aggregateEmotionsByMovie(@Param("movieId") Long movieId);
}
