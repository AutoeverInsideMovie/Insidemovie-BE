package com.insidemovie.backend.api.member.dto.emotion;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemberEmotionSummaryResponseDTO {
    private Long memberId;
    private String repEmotion; // 대표 감정 상태
}
