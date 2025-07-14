package com.insidemovie.backend.api.review.repository;

import com.insidemovie.backend.api.review.entity.Emotion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmotionRespository extends JpaRepository<Emotion, Long> {
}
