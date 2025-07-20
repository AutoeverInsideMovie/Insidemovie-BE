package com.insidemovie.backend.api.report.dto;

import com.insidemovie.backend.api.report.entity.Report;
import com.insidemovie.backend.api.constant.ReportReason;
import com.insidemovie.backend.api.constant.ReportStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReportResponseDTO {

    private Long id;
    private Long reviewId;
    private Long reporterId;
    private Long reportedMemberId;
    private ReportStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private ReportReason reason;

    public static ReportResponseDTO fromEntity(Report report) {
        return ReportResponseDTO.builder()
                .id(report.getId())
                .reviewId(report.getReview().getId())
                .reporterId(report.getReporter().getId())
                .reportedMemberId(report.getReportedMember().getId())
                .reason(report.getReason())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .build();
    }
}
