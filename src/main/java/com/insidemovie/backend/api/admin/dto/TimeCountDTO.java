package com.insidemovie.backend.api.admin.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TimeCountDTO {
    private String date;
    private Long count;
}
