package com.insidemovie.backend.api.movie.entity.boxoffice;

import com.insidemovie.backend.api.movie.entity.Movie;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;


@Entity
@Table(
    name = "daily_box_office",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_daily_target_movie",
            columnNames = {"target_date", "movie_cd"}
        )
    },
    indexes = {
        @Index(name = "idx_daily_target_rank", columnList = "target_date, movie_rank"),
        @Index(name = "idx_daily_tmdb", columnList = "tmdb_id")
    }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class DailyBoxOfficeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TMDB 영화 (nullable: 아직 매핑 전일 수 있음)
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "tmdb_id", referencedColumnName = "tmdb_id",
                foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Movie movie;

    @Column(name = "target_date", nullable = false)
    private LocalDate targetDate;

    private String rnum;
    @Column(name = "movie_rank")
    private String movieRank;
    private String rankInten;
    private String rankOldAndNew;

    @Column(name = "movie_cd", nullable = false)
    private String movieCd;
    @Column(name = "movie_name")
    private String movieName;

    @Column(name = "open_date")
    private String openDate;

    private String salesShare;
    private String salesInten;
    private String salesChange;
    private String salesAcc;
    private String salesAmt;

    private String audiCnt;
    private String audiInten;
    private String audiChange;
    private String audiAcc;

    private String scrnCnt;
    private String showCnt;

    /** 기존 행을 새 API 데이터로 갱신 (변하지 않는 key 컬럼 제외) */
    public void updateFrom(DailyBoxOfficeEntity other) {
        this.rnum          = other.rnum;
        this.movieRank     = other.movieRank;
        this.rankInten     = other.rankInten;
        this.rankOldAndNew = other.rankOldAndNew;
        this.openDate      = other.openDate;
        this.salesShare    = other.salesShare;
        this.salesInten    = other.salesInten;
        this.salesChange   = other.salesChange;
        this.salesAcc      = other.salesAcc;
        this.salesAmt      = other.salesAmt;
        this.audiCnt       = other.audiCnt;
        this.audiInten     = other.audiInten;
        this.audiChange    = other.audiChange;
        this.audiAcc       = other.audiAcc;
        this.scrnCnt       = other.scrnCnt;
        this.showCnt       = other.showCnt;
        // movieCd, movieName, targetDate 는 식별/표시 기본 속성 → 필요 시 유지
    }
}
