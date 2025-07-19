package com.insidemovie.backend.api.constant;

import java.util.Arrays;
import java.util.Optional;

public enum GenreType {
    액션(28),
    모험(12),
    애니메이션(16),
    코미디(35),
    범죄(80),
    다큐멘터리(99),
    드라마(18),
    가족(10751),
    판타지(14),
    역사(36),
    공포(27),
    음악(10402),
    미스터리(9648),
    로맨스(10749),
    SF(878),
    TV영화(10770),
    스릴러(53),
    전쟁(10752),
    서부(37);

    private final int tmdbId;

    GenreType(int tmdbId){
        this.tmdbId = tmdbId;
    }

    public int getId(){
        return tmdbId;
    }
    // ID → enum 조회
    public static Optional<GenreType> fromId(int id) {
        return Arrays.stream(values())
                .filter(g -> g.tmdbId == id)
                .findFirst();
    }
    public static Optional<GenreType> fromName(String name) {
        return Arrays.stream(values())
                .filter(g -> g.name().equalsIgnoreCase(name))
                .findFirst();
    }

}
