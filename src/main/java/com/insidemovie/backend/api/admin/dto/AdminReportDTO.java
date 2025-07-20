package com.insidemovie.backend.api.admin.dto;

import com.insidemovie.backend.api.constant.ReportReason;
import com.insidemovie.backend.api.constant.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminReportDTO {

    private Long reportId;

    private Long reviewId;
    private String reviewContent;

    private Long reporterId;
    private String reporterNickname;

    private Long reportedMemberId;
    private String reportedNickname;

    private ReportReason reason;
    private ReportStatus status;
    private LocalDateTime createdAt;
}
