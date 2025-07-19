package com.insidemovie.backend.api.admin.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AdminDashboardDTO {

    private long totalMembers;  // 총 회원 수
    private long bannedMembers;  // 정지된 회원 수
    private long totalReviews;  // 총 리뷰 수
    private long concealedReviews;  // 신고된 리뷰 수
    private long unprocessedReports;  // 미처리된 신고 수

    // 월별, 일별 가입자 수, 리뷰 수, 신고 수
    private List<TimeCountDTO> dailyMemberCounts;
    private List<TimeCountDTO> monthlyMemberCounts;

    private List<TimeCountDTO> dailyReviewCounts;
    private List<TimeCountDTO> monthlyReviewCounts;

    private List<TimeCountDTO> dailyReportCounts;
    private List<TimeCountDTO> monthlyReportCounts;

    // 월별, 일별 누적 수
    private List<TimeCountDTO> dailyMemberCumulativeCounts;
    private List<TimeCountDTO> monthlyMemberCumulativeCounts;
    private List<TimeCountDTO> dailyReviewCumulativeCounts;
    private List<TimeCountDTO> monthlyReviewCumulativeCounts;
    private List<TimeCountDTO> dailyReportCumulativeCounts;
    private List<TimeCountDTO> monthlyReportCumulativeCounts;

}
