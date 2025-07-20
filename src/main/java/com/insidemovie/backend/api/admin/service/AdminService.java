package com.insidemovie.backend.api.admin.service;

import com.insidemovie.backend.api.admin.dto.AdminDashboardDTO;
import com.insidemovie.backend.api.admin.dto.AdminMemberDTO;
import com.insidemovie.backend.api.admin.dto.AdminReportDTO;
import com.insidemovie.backend.api.admin.dto.TimeCountDTO;
import com.insidemovie.backend.api.member.entity.Member;
import com.insidemovie.backend.api.member.repository.MemberRepository;
import com.insidemovie.backend.api.report.entity.Report;
import com.insidemovie.backend.api.constant.ReportStatus;
import com.insidemovie.backend.api.report.repository.ReportRepository;
import com.insidemovie.backend.api.review.repository.ReviewRepository;
import com.insidemovie.backend.common.exception.NotFoundException;
import com.insidemovie.backend.common.response.ErrorStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final MemberRepository memberRepository;
    private final ReviewRepository reviewRepository;
    private final ReportRepository reportRepository;

    // 회원 목록 조회
    @Transactional
    public Page<AdminMemberDTO> getMembers(String keyword, Pageable pageable) {
        return memberRepository
                .findByEmailContainingOrNicknameContaining(keyword, keyword, pageable)
                .map(this::convertToDto);
    }

    private AdminMemberDTO convertToDto(Member member) {
        long reviewCount = reviewRepository.countByMember(member);  // 리뷰 수 조회

        return AdminMemberDTO.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .reportCount(member.getReportCount())
                .authority(member.getAuthority().name())
                .createdAt(member.getCreatedAt())
                .reviewCount(reviewCount)
                .isBanned(member.getIsBanned())
                .build();
    }

    // 회원 정지
    @Transactional
    public void banMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBERID_EXCEPTION.getMessage()));
        member.setBanned(true);  // 정지 처리
    }

    // 회원 정지 해제
    @Transactional
    public void unbanMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBERID_EXCEPTION.getMessage()));
        member.setBanned(false); // 정지 해제
    }

    // 신고 목록 조회
    @Transactional
    public Page<AdminReportDTO> getAllReports(Pageable pageable) {
        return reportRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    private AdminReportDTO convertToDto(Report report) {
        return AdminReportDTO.builder()
                .reportId(report.getId())
                .reviewId(report.getReview().getId())
                .reviewContent(report.getReview().getContent())
                .reporterId(report.getReporter().getId())
                .reporterNickname(report.getReporter().getNickname())
                .reportedMemberId(report.getReportedMember().getId())
                .reportedNickname(report.getReportedMember().getNickname())
                .reason(report.getReason())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .build();
    }

    // 대시보드 요약
    @Transactional
    public AdminDashboardDTO getDashboardSummary() {

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        YearMonth thisMonth = YearMonth.now();

        // 어제 기준 30일
        LocalDate startDailyBase = yesterday.minusDays(29); // 30일 전 (누적 첫 날)
        LocalDateTime dailyStart = startDailyBase.atStartOfDay();
        LocalDateTime dailyEnd = today.atStartOfDay();     // 오늘 0시 (어제 포함)

        // 지난달까지 12개월
        YearMonth firstMonth = thisMonth.minusMonths(12);    // 12개월 전
        LocalDateTime monthlyStart = firstMonth.atDay(1).atStartOfDay();
        LocalDateTime monthlyEnd = thisMonth.atDay(1).atStartOfDay(); // 이번달 1일 (이번달 제외)

        // 날짜, 월 리스트 생성 (오래된 → 최신)
        List<LocalDate> last30Days = startDailyBase.datesUntil(yesterday.plusDays(1)).toList(); // 30개
        List<YearMonth> last12Months = IntStream.rangeClosed(1, 12)
                .mapToObj(i -> thisMonth.minusMonths(13 - i)) // 12..1 → 12개월 전 ~ 지난달
                .toList();

        // 신규 일별
        List<TimeCountDTO> rawDailyNewMembers = mapToDTO(memberRepository.countMembersDaily(dailyStart, dailyEnd));
        List<TimeCountDTO> rawDailyNewReviews = mapToDTO(reviewRepository.countReviewsDaily(dailyStart, dailyEnd));
        List<TimeCountDTO> rawDailyNewReports = mapToDTO(reportRepository.countReportsDaily(dailyStart, dailyEnd));

        // 신규 월별
        List<TimeCountDTO> rawMonthlyNewMembers = mapToDTO(memberRepository.countMembersMonthly(monthlyStart, monthlyEnd));
        List<TimeCountDTO> rawMonthlyNewReviews = mapToDTO(reviewRepository.countReviewsMonthly(monthlyStart, monthlyEnd));
        List<TimeCountDTO> rawMonthlyNewReports = mapToDTO(reportRepository.countReportsMonthly(monthlyStart, monthlyEnd));

        // (옵션) 신규 배열도 0 채워서 “고정 길이”로 만들고 싶으면 fill 사용
        List<TimeCountDTO> dailyNewMembers = fillDailyZeros(last30Days, rawDailyNewMembers);
        List<TimeCountDTO> dailyNewReviews = fillDailyZeros(last30Days, rawDailyNewReviews);
        List<TimeCountDTO> dailyNewReports = fillDailyZeros(last30Days, rawDailyNewReports);

        List<TimeCountDTO> monthlyNewMembers = fillMonthlyZeros(last12Months, rawMonthlyNewMembers);
        List<TimeCountDTO> monthlyNewReviews = fillMonthlyZeros(last12Months, rawMonthlyNewReviews);
        List<TimeCountDTO> monthlyNewReports = fillMonthlyZeros(last12Months, rawMonthlyNewReports);

        // 누적 일별
        List<TimeCountDTO> dailyMemberCumulative = buildDailyCumulative(last30Days, memberRepository::countByCreatedAtLessThan);
        List<TimeCountDTO> dailyReviewCumulative = buildDailyCumulative(last30Days, reviewRepository::countByCreatedAtLessThan);
        List<TimeCountDTO> dailyReportCumulative = buildDailyCumulative(last30Days, reportRepository::countByCreatedAtLessThan);

        // 누적 월별
        List<TimeCountDTO> monthlyMemberCumulative = buildMonthlyCumulative(last12Months, memberRepository::countByCreatedAtLessThan);
        List<TimeCountDTO> monthlyReviewCumulative = buildMonthlyCumulative(last12Months, reviewRepository::countByCreatedAtLessThan);
        List<TimeCountDTO> monthlyReportCumulative = buildMonthlyCumulative(last12Months, reportRepository::countByCreatedAtLessThan);

        return AdminDashboardDTO.builder()
                .totalMembers(memberRepository.count())
                .bannedMembers(memberRepository.countByIsBannedTrue())
                .totalReviews(reviewRepository.count())
                .concealedReviews(reviewRepository.countByIsConcealedTrue())
                .unprocessedReports(reportRepository.countByStatus(ReportStatus.UNPROCESSED))

                // 신규(증가분)
                .dailyMemberCounts(dailyNewMembers)
                .dailyReviewCounts(dailyNewReviews)
                .dailyReportCounts(dailyNewReports)
                .monthlyMemberCounts(monthlyNewMembers)
                .monthlyReviewCounts(monthlyNewReviews)
                .monthlyReportCounts(monthlyNewReports)

                // 전체(누적)
                .dailyMemberCumulativeCounts(dailyMemberCumulative)
                .dailyReviewCumulativeCounts(dailyReviewCumulative)
                .dailyReportCumulativeCounts(dailyReportCumulative)
                .monthlyMemberCumulativeCounts(monthlyMemberCumulative)
                .monthlyReviewCumulativeCounts(monthlyReviewCumulative)
                .monthlyReportCumulativeCounts(monthlyReportCumulative)
                .build();
    }

    private List<TimeCountDTO> mapToDTO(List<Object[]> raw) {
        return raw.stream()
                .map(r -> TimeCountDTO.builder()
                        .date(r[0].toString())
                        .count(((Number) r[1]).longValue())
                        .build())
                .toList();
    }

    // 신규 일별 없는 날짜는 0 채우기
    private List<TimeCountDTO> fillDailyZeros(List<LocalDate> days, List<TimeCountDTO> raw) {
        Map<String, Long> map = raw.stream()
                .collect(java.util.stream.Collectors.toMap(TimeCountDTO::getDate, TimeCountDTO::getCount));
        return days.stream()
                .map(d -> TimeCountDTO.builder()
                        .date(d.toString())
                        .count(map.getOrDefault(d.toString(), 0L))
                        .build())
                .toList();
    }

    // 신규 월별 없는 월은 0 채우기
    private List<TimeCountDTO> fillMonthlyZeros(List<YearMonth> months, List<TimeCountDTO> raw) {
        Map<String, Long> map = raw.stream()
                .collect(java.util.stream.Collectors.toMap(TimeCountDTO::getDate, TimeCountDTO::getCount));
        return months.stream()
                .map(ym -> TimeCountDTO.builder()
                        .date(ym.toString())
                        .count(map.getOrDefault(ym.toString(), 0L))
                        .build())
                .toList();
    }

    // 누적 일별
    private List<TimeCountDTO> buildDailyCumulative(List<LocalDate> days,
                                                    Function<LocalDateTime, Long> countFn) {
        return days.stream()
                .map(d -> TimeCountDTO.builder()
                        .date(d.toString())
                        .count(countFn.apply(d.plusDays(1).atStartOfDay()))
                        .build())
                .toList();
    }

    // 누적 월별
    private List<TimeCountDTO> buildMonthlyCumulative(List<YearMonth> months,
                                                      Function<LocalDateTime, Long> countFn) {
        return months.stream()
                .map(ym -> TimeCountDTO.builder()
                        .date(ym.toString())
                        .count(countFn.apply(ym.plusMonths(1).atDay(1).atStartOfDay()))
                        .build())
                .toList();
    }

}
