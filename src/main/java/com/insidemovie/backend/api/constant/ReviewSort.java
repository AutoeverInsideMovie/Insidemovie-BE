package com.insidemovie.backend.api.constant;

import org.springframework.data.domain.Sort;

public enum ReviewSort {

    LATEST,    // 최신순
    OLDEST,    // 오래된순
    POPULAR;   // 좋아요 많은 순

    public Sort toSort() {
        switch (this) {
            case OLDEST:
                return Sort.by("createdAt").ascending();
            case POPULAR:
                return Sort.by("likeCount").descending()
                        .and(Sort.by("createdAt").descending());
            case LATEST:
            default:
                return Sort.by("createdAt").descending();
        }
    }
}
