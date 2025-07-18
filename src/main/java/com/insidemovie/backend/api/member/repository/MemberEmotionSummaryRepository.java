package com.insidemovie.backend.api.member.repository;

import com.insidemovie.backend.api.member.entity.MemberEmotionSummary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberEmotionSummaryRepository extends JpaRepository<MemberEmotionSummary, Long> {
    boolean existsByMemberId(Long memberId);
}
