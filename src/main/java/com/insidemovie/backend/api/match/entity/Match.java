package com.insidemovie.backend.api.match.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@Table(name = "`match`")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "match_id")
    private Long id;

    @Column(name = "match_date")
    private LocalDate matchDate;

    @Column(name = "match_number")
    private Integer matchNumber;

    @Column(name = "winner_id")
    private Long winnerId;

    @Builder.Default
    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MovieMatch> movieMatch = new ArrayList<>();
}
