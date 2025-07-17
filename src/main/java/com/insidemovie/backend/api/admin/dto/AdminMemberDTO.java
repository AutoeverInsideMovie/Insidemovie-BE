package com.insidemovie.backend.api.admin.dto;

import com.insidemovie.backend.api.constant.EmotionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminMemberDTO {

    private Long id;
    private String email;
    private String nickname;
    private int reportCount;
    private String authority;
    private LocalDateTime createdAt;

    private boolean isBanned;

    private long reviewCount;
}
