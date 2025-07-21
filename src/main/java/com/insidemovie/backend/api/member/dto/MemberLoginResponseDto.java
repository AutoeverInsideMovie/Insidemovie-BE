package com.insidemovie.backend.api.member.dto;

import com.insidemovie.backend.api.constant.Authority;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberLoginResponseDto {
    private String accessToken;
    private String refreshToken;
    private Authority authority;
}
