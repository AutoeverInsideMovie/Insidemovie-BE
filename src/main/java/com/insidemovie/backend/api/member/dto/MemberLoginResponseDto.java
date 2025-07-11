package com.insidemovie.backend.api.member.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberLoginResponseDto {
    private String accessToken;
    private String refreshToken;
}
