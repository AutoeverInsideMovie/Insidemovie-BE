package com.insidemovie.backend.api.review.repository;

import com.insidemovie.backend.api.member.dto.emotion.EmotionAvgDTO;
import com.insidemovie.backend.api.review.entity.Emotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
            COALESCE(AVG(e.neutral), 0.0)
        )
        FROM Emotion e
        WHERE e.review.member.id = :memberId
    """)
    Optional<EmotionAvgDTO> findAverageEmotionsByMemberId(@Param("memberId") Long memberId);

    @Query("""
        SELECT new com.insidemovie.backend.api.member.dto.emotion.EmotionAvgDTO(
            COALESCE(AVG(e.joy), 0.0),
            COALESCE(AVG(e.sadness), 0.0),
            COALESCE(AVG(e.anger), 0.0),
            COALESCE(AVG(e.fear), 0.0),
            COALESCE(AVG(e.neutral), 0.0)
        )
        FROM Emotion e
        WHERE e.review.movie.id = :movieId
    """)
    Optional<EmotionAvgDTO> findAverageEmotionsByMovieId(@Param("movieId") Long movieId);


}
