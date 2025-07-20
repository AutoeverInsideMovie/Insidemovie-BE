package com.insidemovie.backend.api.movie.repository;

import com.insidemovie.backend.api.constant.EmotionType;
import com.insidemovie.backend.api.constant.GenreType;
import com.insidemovie.backend.api.movie.entity.Movie;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    Optional<Movie> findByTmdbMovieId(Long tmdbMovieId);
    Page<Movie> findAllByOrderByPopularityDesc(Pageable pageable);

    @Query("""
      SELECT m
      FROM Movie m
      WHERE LOWER(REPLACE(m.title, ' ', ''))
        LIKE LOWER(CONCAT('%', REPLACE(:title, ' ', ''), '%'))
    """)
    Page<Movie> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    @Query("""
    SELECT DISTINCT m
      FROM Movie m
      LEFT JOIN MovieGenre mg
        ON m = mg.movie
     WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :q, '%'))
        OR mg.genreType IN :matchedGenres
  """)
    Page<Movie> searchByTitleOrGenre(
            @Param("q") String q,
            @Param("matchedGenres") List<GenreType> matchedGenres,
            Pageable pageable
    );

    @Query("SELECT mg.movie FROM MovieGenre mg WHERE mg.genreType = :genreType ORDER BY mg.movie.releaseDate DESC")
    Page<Movie> findMoviesByGenreTypeOrderByReleaseDateDesc(@Param("genreType") GenreType genreType, Pageable pageable);

    @Query("SELECT mg.movie FROM MovieGenre mg WHERE mg.genreType = :genreType ORDER BY mg.movie.voteAverage DESC")
    Page<Movie> findMoviesByGenreTypeOrderByVoteAverageDesc(@Param("genreType") GenreType genreType, Pageable pageable);

    // 대결할 영화 - 댓글 30개 이상, 이전에 대결을 진행하지 않은 영화를 별점 순으로 3개
    // TODO: 30개 제한은 데이터 이슈로 뺐음
    // AND m.vote_count >= 30
    @Query(value = """
        SELECT m.* 
        FROM movie m
        JOIN movie_emotion_summary me ON m.movie_id = me.movie_id
        WHERE me.dominant_emotion = :emotion
          AND (m.is_matched IS NULL OR m.is_matched = false)
        ORDER BY m.vote_average DESC
        LIMIT 3
        """, nativeQuery = true)
    List<Movie> findTop3ByEmotion(@Param("emotion") EmotionType emotion);

    // 데이터가 없으면 랜덤한 영화 3개
    @Query(value = "SELECT * FROM movie ORDER BY RAND() LIMIT 3", nativeQuery = true)
    List<Movie> find3Movie(@Param("emotion") EmotionType emotion);
}
