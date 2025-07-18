package com.insidemovie.backend.api.member.dto;

import com.insidemovie.backend.api.constant.EmotionType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProfileEmotionUpdateRequestDto {
    private EmotionType profileEmotion;
}
