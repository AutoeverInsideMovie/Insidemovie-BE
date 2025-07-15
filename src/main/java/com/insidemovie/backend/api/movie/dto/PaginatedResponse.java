package com.insidemovie.backend.api.movie.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PaginatedResponse<T>{
    private int page;
    private List<T> results;
}

