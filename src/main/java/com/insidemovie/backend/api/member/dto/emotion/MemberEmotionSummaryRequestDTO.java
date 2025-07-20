package com.insidemovie.backend.api.member.dto.emotion;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MemberEmotionSummaryRequestDTO {
    private Long memberId;

    @NotNull @Min(0) @Max(1)
    private Float joy;

    @NotNull @Min(0) @Max(1)
    private Float sadness;

    @NotNull @Min(0) @Max(1)
    private Float fear;

    @NotNull @Min(0) @Max(1)
    private Float anger;

    @NotNull @Min(0) @Max(1)
    private Float disgust;
}
