package com.insidemovie.backend.api.member.dto;

import com.insidemovie.backend.api.constant.Authority;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberInfoDto {
    private Long memberId;
    private String email;
    private String nickname;
    private Integer reportCount;
    private Authority authority;
}
