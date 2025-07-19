package com.insidemovie.backend.common.response;

import org.springframework.data.domain.Page;
import java.util.List;

public record PageResult<T>(
        int page,           // 현재 페이지 번호 (0-based)
        int size,           // 페이지 크기
        long totalElements, // 전체 아이템 수
        int totalPages,     // 전체 페이지 수
        boolean last,       // 마지막 페이지 여부
        List<T> content     // 실제 데이터
) {
    public static <T> PageResult<T> of(Page<T> p) {
        return new PageResult<>(
                p.getNumber(),
                p.getSize(),
                p.getTotalElements(),
                p.getTotalPages(),
                p.isLast(),
                p.getContent()
        );
    }
}