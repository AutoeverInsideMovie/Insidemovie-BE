package com.insidemovie.backend.api.recommend.service;

import com.insidemovie.backend.api.movie.entity.MovieEmotionSummary;
import com.insidemovie.backend.api.movie.repository.MovieEmotionSummaryRepository;
import com.insidemovie.backend.api.recommend.dto.EmotionRequestDTO;
import com.insidemovie.backend.api.recommend.dto.MovieRecommendationDTO;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmotionRecommendationService {

    private final MovieEmotionSummaryRepository movieEmotionSummaryRepository;

    // 사용자의 감정 벡터를 기반으로 영화 추천 리스트 반환
    public List<MovieRecommendationDTO> recommendByEmotion(EmotionRequestDTO userEmotion) {

        // 사용자 감정 벡터 정규화 (총합이 100이 아니여서)
        Map<String, Double> userVector = normalize(Map.of(
                "joy", userEmotion.getJoy(),
                "anger", userEmotion.getAnger(),
                "fear", userEmotion.getFear(),
                "disgust", userEmotion.getDisgust(),
                "sadness", userEmotion.getSadness()
        ));

        // DB에서 영화 전체 감정 벡터 불러오기
        List<MovieEmotionSummary> allMovies = movieEmotionSummaryRepository.findAll();

        // 각 영화의 감정 벡터와 유사도 계산
        return allMovies.stream()
                .map(movie -> {

                    // 영화 감정 벡터도 정규화
                    Map<String, Double> movieVector = normalize(Map.of(
                            "joy", movie.getJoy().doubleValue(),
                            "sadness", movie.getSadness().doubleValue(),
                            "anger", movie.getAnger().doubleValue(),
                            "fear", movie.getFear().doubleValue(),
                            "disgust", movie.getDisgust().doubleValue()
                    ));

                    // 유사도 계산
                    double similarity = cosineSimilarity(userVector, movieVector);

                    // 대표 감정 비율 가져오기
                    double dominantRatio = switch (movie.getDominantEmotion()) {
                        case JOY -> movie.getJoy().doubleValue();
                        case SADNESS -> movie.getSadness().doubleValue();
                        case ANGER -> movie.getAnger().doubleValue();
                        case FEAR -> movie.getFear().doubleValue();
                        case DISGUST -> movie.getDisgust().doubleValue();
                        case NONE -> 0.0;
                        default -> 0;
                    };

                    return new MovieRecommendationDTO(
                            movie.getMovieId(),
                            movie.getMovie().getTitle(),
                            movie.getMovie().getPosterPath(),
                            movie.getMovie().getVoteAverage(),
                            movie.getDominantEmotion(),
                            dominantRatio,
                            similarity
                    );
                })

                // 유사도 높은 순으로 정렬
                .sorted(Comparator.comparingDouble(MovieRecommendationDTO::getSimilarity).reversed()) // 유사도 높은 순
                .limit(5) // 상위 5개만 추천
                .collect(Collectors.toList());
    }

    // 벡터 정규화
    private Map<String, Double> normalize(Map<String, Double> vector) {
        double sum = vector.values().stream().mapToDouble(Double::doubleValue).sum();
        return vector.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> sum == 0 ? 0.0 : e.getValue() / sum
                ));
    }

    // 코사인 유사도 계산
    private double cosineSimilarity(Map<String, Double> v1, Map<String, Double> v2) {
        double dot = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (String key : v1.keySet()) {
            double a = v1.get(key);
            double b = v2.getOrDefault(key, 0.0);
            dot += a * b;
            norm1 += a * a;
            norm2 += b * b;
        }

        return (norm1 == 0 || norm2 == 0) ? 0.0 : dot / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}
