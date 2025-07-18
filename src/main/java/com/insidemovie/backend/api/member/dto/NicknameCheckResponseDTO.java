package com.insidemovie.backend.api.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NicknameCheckResponseDTO {
    private boolean isDuplicated;
}
