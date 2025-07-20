package com.insidemovie.backend.api.movie.repository;

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
    @Query(value = """
            SELECT * FROM movie m
            JOIN movie_emotion_summary me ON m.movie_id = me.movie_id
            WHERE me.dominant_emotion = :emotion
                AND m.vote_count >= 30
                AND m.movie_id NOT IN (
                    SELECT mf.movie_id FROM movie_fight mf
                )
            ORDER BY m.vote_average DESC
            LIMIT 3
            """, nativeQuery = true)
    List<Movie> findTop3ByEmotion(String emotion);
}
