package com.insidemovie.backend.api.member.dto.emotion;

import com.insidemovie.backend.api.constant.EmotionType;
import com.insidemovie.backend.api.member.entity.MemberEmotionSummary;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemberEmotionSummaryResponseDTO {
    private Double joy;
    private Double sadness;
    private Double anger;
    private Double fear;
    private Double disgust;
    private EmotionType repEmotion;

    /**
     * 엔티티 → 응답 DTO 변환용 팩토리 메서드
     */
    public static MemberEmotionSummaryResponseDTO fromEntity(MemberEmotionSummary e) {
        return MemberEmotionSummaryResponseDTO.builder()
            .joy(e.getJoy().doubleValue())
            .sadness(e.getSadness().doubleValue())
            .anger(e.getAnger().doubleValue())
            .fear(e.getFear().doubleValue())
            .disgust(e.getDisgust().doubleValue())
            .repEmotion(e.getRepEmotionType())
            .build();
    }
}
