package com.insidemovie.backend.api.constant;

public enum ReportReason {
    INAPPROPRIATE_LANGUAGE, // 부적절한 언어 사용 (욕설 / 비방)
    SEXUAL_CONTENT,         // 성적인 불쾌감 유발 (성희롱)
    SPOILER,                // 줄거리 노출 (스포일러)
    RUDE_BEHAVIOR,          // 무례하거나 공격적인 태도 (비매너)
    ADVERTISEMENT           // 광고 또는 홍보성 내용 (광고)
}