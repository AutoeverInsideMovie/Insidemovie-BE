package com.insidemovie.backend.api.admin.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminDashboardDTO {

    private long totalMembers;  // 총 회원 수
    private long bannedMembers;  // 정지된 회원 수
    private long totalReviews;  // 총 리뷰 수
    private long concealedReviews;  // 신고된 리뷰 수
    private long unprocessedReports;  // 미처리된 신고 수
}
