package com.insidemovie.backend.api.member.dto;

import com.insidemovie.backend.api.constant.EmotionType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MainEmotionUpdateRequestDTO {

    @NotNull(message = "감정을 선택해주세요.")
    private EmotionType mainEmotion;
}
