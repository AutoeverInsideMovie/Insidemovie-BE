package com.insidemovie.backend.api.movie.entity.boxoffice;

import com.insidemovie.backend.api.movie.entity.Movie;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "weekly_box_office",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_weekly_yearweek_movie",
            columnNames = {"year_week_time", "movie_cd"}
        )
    },
    indexes = {
        @Index(name = "idx_weekly_yearweek_rank", columnList = "year_week_time, movie_rank"),
        @Index(name = "idx_weekly_tmdb", columnList = "tmdb_id")
    }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class WeeklyBoxOfficeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "tmdb_id", referencedColumnName = "tmdb_id",
                foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Movie movie;

    @Column(name = "year_week_time", nullable = false)
    private String yearWeekTime;

    private String rnum;
    @Column(name = "movie_rank")
    private String movieRank;
    private String rankInten;
    private String rankOldAndNew;

    @Column(name = "movie_cd", nullable = false)
    private String movieCd;
    @Column(name = "movie_nm")
    private String movieNm;

    private String openDt;

    private String salesAmt;
    private String salesShare;
    private String salesInten;
    private String salesChange;
    private String salesAcc;

    private String audiCnt;
    private String audiInten;
    private String audiChange;
    private String audiAcc;

    private String scrnCnt;
    private String showCnt;

    public void updateFrom(WeeklyBoxOfficeEntity other) {
        this.rnum          = other.rnum;
        this.movieRank     = other.movieRank;
        this.rankInten     = other.rankInten;
        this.rankOldAndNew = other.rankOldAndNew;
        this.openDt        = other.openDt;
        this.salesAmt      = other.salesAmt;
        this.salesShare    = other.salesShare;
        this.salesInten    = other.salesInten;
        this.salesChange   = other.salesChange;
        this.salesAcc      = other.salesAcc;
        this.audiCnt       = other.audiCnt;
        this.audiInten     = other.audiInten;
        this.audiChange    = other.audiChange;
        this.audiAcc       = other.audiAcc;
        this.scrnCnt       = other.scrnCnt;
        this.showCnt       = other.showCnt;
    }
}
