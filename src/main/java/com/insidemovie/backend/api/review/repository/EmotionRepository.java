package com.insidemovie.backend.api.review.repository;

import com.insidemovie.backend.api.member.dto.emotion.EmotionAvgDTO;
import com.insidemovie.backend.api.review.entity.Emotion;
import com.insidemovie.backend.api.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface EmotionRepository extends JpaRepository<Emotion, Long> {

    Optional<Emotion> findByReviewId(Long reviewId);

    // 멤버가 작성한 모든 리뷰 감정의 평균값 계산
    @Query("""
        SELECT new com.insidemovie.backend.api.member.dto.emotion.EmotionAvgDTO(
            COALESCE(AVG(e.joy), 0.0),
            COALESCE(AVG(e.sadness), 0.0),
            COALESCE(AVG(e.anger), 0.0),
            COALESCE(AVG(e.fear), 0.0),
            COALESCE(AVG(e.disgust), 0.0)
        )
        FROM Emotion e
        WHERE e.review.member.id = :memberId
    """)
    Optional<EmotionAvgDTO> findAverageEmotionsByMemberId(@Param("memberId") Long memberId);

    // 영화의 모든 리뷰 감정의 평균값 계산
    @Query("""
        SELECT new com.insidemovie.backend.api.member.dto.emotion.EmotionAvgDTO(
            COALESCE(AVG(e.joy), 0.0),
            COALESCE(AVG(e.sadness), 0.0),
            COALESCE(AVG(e.anger), 0.0),
            COALESCE(AVG(e.fear), 0.0),
            COALESCE(AVG(e.disgust), 0.0)
        )
        FROM Emotion e
        WHERE e.review.movie.id = :movieId
    """)
    Optional<EmotionAvgDTO> findAverageEmotionsByMovieId(@Param("movieId") Long movieId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM Emotion e WHERE e.review = :review")
    void deleteByReview(@Param("review") Review review);

    /**
     * 주어진 영화 ID 목록에 속하는 모든 Emotion 레코드의
     * joy, sadness, anger, fear, disgust 평균을 한 번에 계산한다.
     */
    @Query("""
        SELECT new com.insidemovie.backend.api.member.dto.emotion.EmotionAvgDTO(
            COALESCE(AVG(e.joy), 0.0),
            COALESCE(AVG(e.sadness), 0.0),
            COALESCE(AVG(e.anger), 0.0),
            COALESCE(AVG(e.fear), 0.0),
            COALESCE(AVG(e.disgust), 0.0)
        )
        FROM Emotion e
        WHERE e.review.movie.id IN :movieIds
    """)
    Optional<EmotionAvgDTO> findAverageEmotionsByMovieIds(@Param("movieIds") List<Long> movieIds);
}
