package com.insidemovie.backend.api.report.dto;

import com.insidemovie.backend.api.report.entity.ReportReason;
import lombok.Getter;

@Getter
public class ReportRequestDTO {

    private ReportReason reason;
}
