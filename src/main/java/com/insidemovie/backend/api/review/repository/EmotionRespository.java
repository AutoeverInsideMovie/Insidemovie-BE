package com.insidemovie.backend.api.review.repository;

import com.insidemovie.backend.api.review.entity.Emotion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmotionRespository extends JpaRepository<Emotion, Long> {
    Optional<Emotion> findByReviewId(Long reviewId);
}
